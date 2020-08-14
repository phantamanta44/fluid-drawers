package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntitySlave;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Objects;

public class FluidControllerProxy implements IFluidHandler, ICapabilityProvider {

    private static final IFluidTankProperties[] NO_TANKS = new IFluidTankProperties[0]; // i'm good, tank you very much

    private final TileEntitySlave tile;

    public FluidControllerProxy(TileEntitySlave tile) {
        this.tile = tile;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        IFluidHandler ctrlerFluidHandler = getControllerFluidHandler();
        return ctrlerFluidHandler != null ? ctrlerFluidHandler.getTankProperties() : NO_TANKS;
    }

    @Override
    public int fill(FluidStack fluid, boolean commit) {
        IFluidHandler ctrlerFluidHandler = getControllerFluidHandler();
        return ctrlerFluidHandler != null ? ctrlerFluidHandler.fill(fluid, commit) : 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluid, boolean commit) {
        IFluidHandler ctrlerFluidHandler = getControllerFluidHandler();
        return ctrlerFluidHandler != null ? ctrlerFluidHandler.drain(fluid, commit) : null;
    }

    @Nullable
    @Override
    public FluidStack drain(int amount, boolean commit) {
        IFluidHandler ctrlerFluidHandler = getControllerFluidHandler();
        return ctrlerFluidHandler != null ? ctrlerFluidHandler.drain(amount, commit) : null;
    }

    @Nullable
    private IFluidHandler getControllerFluidHandler() {
        TileEntityController ctrler = tile.getController();
        return ctrler != null && ctrler.isValidSlave(tile.getPos())
                && ctrler.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
                ? Objects.requireNonNull(ctrler.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                : null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this) : null;
    }

}
