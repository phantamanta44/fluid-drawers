package xyz.phanta.fluiddrawers.inventory.slot;

import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
        ItemStack stack = getStack();

        if (stack.isEmpty()) {
            return true;
        }

        if (!inv.canTakeStack(getSlotIndex())) {
            return false;
        }

        return stack.getItem() != ModItems.upgradeCreative
                || player == null
                || player.capabilities.isCreativeMode;
    }

}
