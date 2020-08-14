package xyz.phanta.fluiddrawers.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class BypassingFluidHandlerWrapper implements IFluidHandler {

    private final BypassableFluidHandler delegate;

    public BypassingFluidHandlerWrapper(BypassableFluidHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return delegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack fluid, boolean commit) {
        return delegate.fill(fluid, commit, true);
    }

    @Override
    @Nullable
    public FluidStack drain(FluidStack fluid, boolean commit) {
        return delegate.drain(fluid, commit, true);
    }

    @Override
    @Nullable
    public FluidStack drain(int amount, boolean commit) {
        return delegate.drain(amount, commit, true);
    }

}
