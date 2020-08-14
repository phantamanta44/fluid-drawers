package xyz.phanta.fluiddrawers.handler;

import com.jaquadro.minecraft.storagedrawers.block.BlockController;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntitySlave;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.drawers.FluidControllerProxy;
import xyz.phanta.fluiddrawers.drawers.FluidDrawerController;

import java.util.Objects;

public class ControllerFluidCapabilityHandler {

    private static final ResourceLocation CAP_FLUID_CTRL = FluidDrawersMod.INSTANCE.newResourceLocation("fluid_ctrl");
    private static final ResourceLocation CAP_FLUID_PROXY = FluidDrawersMod.INSTANCE.newResourceLocation("fluid_proxy");

    @SubscribeEvent
    public void onTileCapabilities(AttachCapabilitiesEvent<TileEntity> event) {
        TileEntity tile = event.getObject();
        if (tile instanceof TileEntityController) {
            event.addCapability(CAP_FLUID_CTRL, new FluidDrawerController((TileEntityController)tile));
        } else if (tile instanceof TileEntitySlave) {
            event.addCapability(CAP_FLUID_PROXY, new FluidControllerProxy((TileEntitySlave)tile));
        }
    }

    @SubscribeEvent
    public void onInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        BlockPos pos = event.getPos();
        EnumFacing face = event.getFace();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != ModBlocks.controller || face != state.getValue(BlockController.FACING)) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityController)
                || !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        EnumHand hand = event.getHand();
        ItemStack container = player.getHeldItem(hand);
        if (container.isEmpty()) {
            return;
        }
        FluidActionResult result = FluidUtil.tryEmptyContainer(container,
                Objects.requireNonNull(tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)),
                Integer.MAX_VALUE, player, true);
        if (result.success) {
            if (!player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, result.result);
            }
            event.setCanceled(true);
        }
    }

}
