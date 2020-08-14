package xyz.phanta.fluiddrawers.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public interface BypassableFluidHandler extends IFluidHandler {

    int fill(FluidStack fluid, boolean commit, boolean bypass);

    @Override
    default int fill(FluidStack fluid, boolean commit) {
        return fill(fluid, commit, false);
    }

    @Nullable
    FluidStack drain(FluidStack fluid, boolean commit, boolean bypass);

    @Nullable
    @Override
    default FluidStack drain(FluidStack fluid, boolean commit) {
        return drain(fluid, commit, false);
    }

    @Nullable
    FluidStack drain(int amount, boolean commit, boolean bypass);

    @Nullable
    @Override
    default FluidStack drain(int amount, boolean commit) {
        return drain(amount, commit, false);
    }

}
