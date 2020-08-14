package xyz.phanta.fluiddrawers.client.gui;

import io.github.phantamanta44.libnine.client.gui.L9GuiContainer;
import io.github.phantamanta44.libnine.client.gui.component.impl.GuiComponentFluidTank;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.constant.NameConst;
import xyz.phanta.fluiddrawers.inventory.ContainerTank;
import xyz.phanta.fluiddrawers.util.DrawerTankWrapper;

public class GuiTank extends L9GuiContainer {

    private static final ResourceLocation BG = FluidDrawersMod.INSTANCE.newResourceLocation("textures/gui/tank.png");

    public GuiTank(ContainerTank cont) {
        super(cont, BG, 176, 199);
        addComponent(new GuiComponentFluidTank(80, 36, 16, 16, new DrawerTankWrapper(cont.getFluidDrawer())));
    }

    @Override
    public void drawBackground(float partialTicks, int mX, int mY) {
        super.drawBackground(partialTicks, mX, mY);
        for (int i = 0; i < 7; i++) {
            Slot slot = inventorySlots.getSlot(36 + i);
            if (!slot.canTakeStack(mc.player)) {
                drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, 176, 0, 16, 16);
            }
        }
    }

    @Override
    public void drawForeground(float partialTicks, int mX, int mY) {
        super.drawForeground(partialTicks, mX, mY);
        drawContainerName(I18n.format(NameConst.CONT_TANK));
        fontRenderer.drawString(I18n.format("storagedrawers.container.upgrades"), 8, 75, DEF_TEXT_COL);
    }

}
