package xyz.phanta.fluiddrawers.client.model.base;

import io.github.phantamanta44.libnine.util.render.model.NoopOverrideList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class DelegatingBakedModel implements IBakedModel {

    @Nullable
    protected IBakedModel delegate;

    public DelegatingBakedModel(@Nullable IBakedModel delegate) {
        this.delegate = delegate;
    }

    public DelegatingBakedModel() {
        this(null);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return delegate != null ? delegate.getOverrides() : NoopOverrideList.INSTANCE;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return delegate != null ? delegate.getQuads(state, side, rand) : Collections.emptyList();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return delegate != null
                ? delegate.getParticleTexture() : Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return delegate != null && delegate.isBuiltInRenderer();
    }

    @Override
    public boolean isGui3d() {
        return delegate != null && delegate.isGui3d();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return delegate != null && delegate.isAmbientOcclusion(state);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return delegate != null && delegate.isAmbientOcclusion();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return delegate != null ? delegate.getItemCameraTransforms() : ItemCameraTransforms.DEFAULT;
    }

}
