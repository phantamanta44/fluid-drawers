package xyz.phanta.fluiddrawers.handler;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import io.github.phantamanta44.libnine.util.nullity.Reflected;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import xyz.phanta.fluiddrawers.drawers.FluidDrawerController;
import xyz.phanta.fluiddrawers.item.base.FramedItem;

// no reflection is used here; these functions are called from code injected by the coremod
public class FluidDrawersCoreHooks {

    @Reflected
    public static void updateFluidControllerCache(TileEntityController tile) {
        IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (fluidHandler instanceof FluidDrawerController) {
            ((FluidDrawerController)fluidHandler).rebuildCache();
        }
    }

    @Reflected
    public static ItemStack getFramedItemStack(ItemStack target, ItemStack matSide, ItemStack matTrim, ItemStack matFront) {
        if (matSide.isEmpty() || target.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Item item = target.getItem();
        return item instanceof FramedItem
                ? ((FramedItem)item).createFramedStack(target, matSide, matTrim, matFront) : ItemStack.EMPTY;
    }

}
