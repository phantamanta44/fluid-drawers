package xyz.phanta.fluiddrawers.network;

import io.github.phantamanta44.libnine.util.nullity.Reflected;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.tile.TileTank;

import javax.annotation.Nullable;

@SuppressWarnings("NotNullFieldNotInitialized")
public class SPacketSyncFluidDrawerCount implements IMessage {

    private BlockPos pos;
    private int count;

    public SPacketSyncFluidDrawerCount(BlockPos pos, int count) {
        this.pos = pos;
        this.count = count;
    }

    public SPacketSyncFluidDrawerCount() {
        // NO-OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(count);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        count = buf.readInt();
    }

    @Reflected
    public static class Handler implements IMessageHandler<SPacketSyncFluidDrawerCount, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketSyncFluidDrawerCount message, MessageContext ctx) {
            //noinspection Convert2Lambda
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(message.pos);
                    if (tile instanceof TileTank) {
                        FluidDrawer drawer = ((TileTank)tile).getFluidDrawerGroup().getFluidDrawer();
                        FluidStack fluid = drawer.getStoredFluid();
                        if (fluid != null) {
                            fluid = fluid.copy();
                            fluid.amount = Math.max(message.count, 0);
                            drawer.setStoredFluid(fluid);
                        }
                    }
                }
            });
            return null;
        }

    }

}
