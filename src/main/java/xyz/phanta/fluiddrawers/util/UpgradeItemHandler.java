package xyz.phanta.fluiddrawers.util;

import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import xyz.phanta.fluiddrawers.drawers.DrawerUpgradable;

public class UpgradeItemHandler implements IItemHandlerModifiable {

    private final DrawerUpgradable tile;

    public UpgradeItemHandler(DrawerUpgradable tile) {
        this.tile = tile;
    }

    @Override
    public int getSlots() {
        return tile.getUpgrades().getSlotCount();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return tile.getUpgrades().getUpgrade(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        tile.getUpgrades().setUpgrade(slot, stack);
        tile.notifyChanged();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        UpgradeData upgrades = tile.getUpgrades();
        if (upgrades.getUpgrade(slot).isEmpty()) {
            stack = stack.copy();
            setStackInSlot(slot, stack.splitStack(1));
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = tile.getUpgrades().getUpgrade(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            setStackInSlot(slot, ItemStack.EMPTY);
            stack.setCount(1); // precaution
            return stack;
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return tile.getUpgrades().canAddUpgrade(stack);
    }

    public boolean canTakeStack(int slot) {
        return tile.getUpgrades().canRemoveUpgrade(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

}
