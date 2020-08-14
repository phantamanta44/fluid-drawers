package xyz.phanta.fluiddrawers.client.tesr;

import io.github.phantamanta44.libnine.util.math.MathUtils;
import io.github.phantamanta44.libnine.util.render.FluidRenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.tile.TileTank;

public class RenderTileTank<T extends TileTank> extends TileEntitySpecialRenderer<T> {

    private static final double d = 0.375D;

    // adapted from ae2fc
    @Override
    public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile.getAttributes().isConcealed()) {
            return;
        }
        FluidDrawer drawer = tile.getFluidDrawerGroup().getFluidDrawer();
        FluidStack fluid = drawer.getStoredFluid();
        if (fluid == null || fluid.amount == 0) {
            return;
        }

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int lightCoords = getWorld().getCombinedLight(tile.getPos(), fluid.getFluid().getLuminosity(fluid));
        renderFluid(fluid, drawer.getMaxCapacity(), tile.getAttributes().isUnlimitedVending(),
                (lightCoords >> 16) & 0xFFFF, lightCoords & 0xFFFF);

        if (tile.getAttributes().isShowingQuantity()) {
            FontRenderer fr = getFontRenderer();
            String qtyLabel = String.format("%,d mB", fluid.amount);
            float halfWidth = fr.getStringWidth(qtyLabel) / 2F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(1F / 128F, -1F / 128F, 1F / 128F);
            GlStateManager.rotate(180F, 0F, 1F, 0F);
            GlStateManager.translate(-64F - halfWidth, -8F - fr.FONT_HEIGHT / 2F, 0.01F); // extra 0.01 fixes z-fighting
            fr.drawString(qtyLabel, 0, 0, 0xFFFFFFFF);
            for (int i = 0; i < 3; i++) {
                GlStateManager.translate(64F + halfWidth + 0.01F, 0F, -64F + halfWidth - 0.01F);
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                fr.drawString(qtyLabel, 0, 0, 0xFFFFFFFF);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    public static void renderFluid(FluidStack fluid, int fluidCapacity, boolean vending, int lMapX, int lMapY) {
        TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(fluid);
        if (sprite == null) {
            return;
        }

        float fillPercent;
        float alpha;
        if (vending) {
            fillPercent = 1F;
            alpha = 1F;
        } else if (fluid.getFluid().isGaseous() || fluid.getFluid().isLighterThanAir()) {
            fillPercent = 1F;
            alpha = Math.max(MathUtils.clamp(fluid.amount / (float)fluidCapacity, 0F, 1F), 0.01F);
        } else {
            // lower bound of 0.01 avoids z-fighting for when fill percentage is very low (e.g. with creative cap upgrade)
            fillPercent = Math.max(MathUtils.clamp(fluid.amount / (float)fluidCapacity, 0F, 1F), 0.01F);
            alpha = 1F;
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5D, 0.5D - d, 0.5D);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();
        drawFluidSide(tess, buf, sprite, fillPercent, alpha, lMapX, lMapY);
        for (int i = 0; i < 3; i++) {
            GlStateManager.rotate(90F, 0F, 1F, 0F);
            drawFluidSide(tess, buf, sprite, fillPercent, alpha, lMapX, lMapY);
        }
        GlStateManager.popMatrix();

        if (fillPercent < 1F) {
            double yMax = fillPercent * (d + d);
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
            buf.pos(-d, yMax, -d).tex(sprite.getMinU(), sprite.getMinV()).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
            buf.pos(-d, yMax, d).tex(sprite.getMinU(), sprite.getMaxV()).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
            buf.pos(d, yMax, d).tex(sprite.getMaxU(), sprite.getMaxV()).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
            buf.pos(d, yMax, -d).tex(sprite.getMaxU(), sprite.getMinV()).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
            tess.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private static void drawFluidSide(Tessellator tess, BufferBuilder buf, TextureAtlasSprite sprite,
                                      float height, float alpha, int lMapX, int lMapY) {
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        double uMin = sprite.getInterpolatedU(d * 8D), uMax = sprite.getInterpolatedU(16D - d * 8D);
        double vMin = sprite.getMinV(), vMax = sprite.getInterpolatedV(height * 16D);
        double yMax = height * (d + d);
        buf.pos(-d, yMax, d).tex(uMin, vMin).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
        buf.pos(-d, 0D, d).tex(uMin, vMax).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
        buf.pos(d, 0D, d).tex(uMax, vMax).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
        buf.pos(d, yMax, d).tex(uMax, vMin).lightmap(lMapX, lMapY).color(1F, 1F, 1F, alpha).endVertex();
        tess.draw();
    }

}
