package xyz.phanta.fluiddrawers.network;

import io.github.phantamanta44.libnine.util.nullity.Reflected;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.tile.TileTank;

import javax.annotation.Nullable;
import java.io.IOException;

@SuppressWarnings("NotNullFieldNotInitialized")
public class SPacketSyncFluidDrawerFluid implements IMessage {

    private BlockPos pos;
    @Nullable
    private FluidStack fluid;

    public SPacketSyncFluidDrawerFluid(BlockPos pos, @Nullable FluidStack fluid) {
        this.pos = pos;
        this.fluid = fluid;
    }

    public SPacketSyncFluidDrawerFluid() {
        // NO-OP
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pktBuf = new PacketBuffer(buf);
        pktBuf.writeBlockPos(pos);
        if (fluid != null && fluid.amount > 0) {
            pktBuf.writeInt(fluid.amount);
            pktBuf.writeString(fluid.getFluid().getName());
            if (fluid.tag != null) {
                pktBuf.writeBoolean(false);
            } else {
                pktBuf.writeBoolean(true);
                pktBuf.writeCompoundTag(fluid.tag);
            }
        } else {
            pktBuf.writeInt(0);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pktBuf = new PacketBuffer(buf);
        pos = pktBuf.readBlockPos();
        int fluidAmount = pktBuf.readInt();
        if (fluidAmount > 0) {
            try {
                // i will personally place a curse on any modder that uses a fluid name > 255 chars
                Fluid fluidType = FluidRegistry.getFluid(pktBuf.readString(255));
                NBTTagCompound tag = pktBuf.readBoolean() ? pktBuf.readCompoundTag() : null;
                if (fluidType != null) {
                    fluid = new FluidStack(fluidType, fluidAmount, tag);
                } else {
                    fluid = null;
                }
            } catch (IOException e) {
                throw new EncoderException(e);
            }
        } else {
            fluid = null;
        }
    }

    @Reflected
    public static class Handler implements IMessageHandler<SPacketSyncFluidDrawerFluid, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketSyncFluidDrawerFluid message, MessageContext ctx) {
            //noinspection Convert2Lambda
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(message.pos);
                    if (tile instanceof TileTank) {
                        FluidDrawer drawer = ((TileTank)tile).getFluidDrawerGroup().getFluidDrawer();
                        if (message.fluid != null) {
                            if (message.fluid.amount < 0) {
                                message.fluid.amount = 0;
                            }
                            drawer.setStoredFluid(message.fluid);
                        } else {
                            drawer.setStoredFluid(null);
                        }
                    }
                }
            });
            return null;
        }

    }

}
