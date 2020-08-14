package xyz.phanta.fluiddrawers.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Objects;

public class BlockInteractionUtils {

    public static boolean transferFluid(ICapabilityProvider dest, EntityPlayer player, EnumHand hand,
                                        @Nullable EnumFacing face, boolean bypass) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || !dest.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)) {
            return false;
        }
        IFluidHandler tank = Objects.requireNonNull(dest.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face));
        if (bypass && tank instanceof BypassableFluidHandler) {
            tank = new BypassingFluidHandlerWrapper((BypassableFluidHandler)tank);
        }
        FluidActionResult result = FluidUtil.tryFillContainer(stack, tank, Integer.MAX_VALUE, player, true);
        if (result.success) {
            if (!player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, result.result);
            }
            return true;
        }
        result = FluidUtil.tryEmptyContainer(stack, tank, Integer.MAX_VALUE, player, true);
        if (result.success) {
            if (!player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, result.result);
            }
            return true;
        }
        return false;
    }

}
