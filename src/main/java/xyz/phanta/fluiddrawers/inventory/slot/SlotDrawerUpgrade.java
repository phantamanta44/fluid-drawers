package xyz.phanta.fluiddrawers.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.SlotItemHandler;
import xyz.phanta.fluiddrawers.util.UpgradeItemHandler;

public class SlotDrawerUpgrade extends SlotItemHandler {

    private final UpgradeItemHandler inv;

    public SlotDrawerUpgrade(UpgradeItemHandler inv, int index, int posX, int posY) {
        super(inv, index, posX, posY);
        this.inv = inv;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return getStack().isEmpty() || inv.canTakeStack(getSlotIndex());
    }

}
