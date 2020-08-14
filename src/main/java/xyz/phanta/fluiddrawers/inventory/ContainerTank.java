package xyz.phanta.fluiddrawers.inventory;

import io.github.phantamanta44.libnine.gui.L9Container;
import net.minecraft.entity.player.EntityPlayer;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.inventory.slot.SlotDrawerUpgrade;
import xyz.phanta.fluiddrawers.tile.TileTank;
import xyz.phanta.fluiddrawers.util.UpgradeItemHandler;

public class ContainerTank extends L9Container {

    private final TileTank tile;

    public ContainerTank(EntityPlayer player, TileTank tile) {
        super(player.inventory, 199);
        this.tile = tile;
        UpgradeItemHandler upgradeInv = new UpgradeItemHandler(tile);
        for (int i = 0; i < 7; i++) {
            addSlotToContainer(new SlotDrawerUpgrade(upgradeInv, i, 26 + i * 18, 86));
        }
    }

    public FluidDrawer getFluidDrawer() {
        return tile.getFluidDrawerGroup().getFluidDrawer();
    }

}
