package xyz.phanta.fluiddrawers.integration;

import eutros.framedcompactdrawers.block.BlockControllerCustom;
import eutros.framedcompactdrawers.registry.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.phanta.fluiddrawers.handler.ControllerFluidCapabilityHandler;

public class FramedDrawerHandler {

    @SubscribeEvent
    public void onInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EnumFacing face = event.getFace();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != ModBlocks.framedDrawerController || face != state.getValue(BlockControllerCustom.FACING)) {
            return;
        }
        if (ControllerFluidCapabilityHandler
                .handleTankInteraction(world.getTileEntity(pos), face, event.getEntityPlayer(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

}
