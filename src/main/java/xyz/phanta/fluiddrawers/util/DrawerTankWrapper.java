package xyz.phanta.fluiddrawers.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;

import javax.annotation.Nullable;

public class DrawerTankWrapper implements IFluidTank {

    private final FluidDrawer drawer;

    public DrawerTankWrapper(FluidDrawer drawer) {
        this.drawer = drawer;
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return drawer.getStoredFluid();
    }

    @Override
    public int getFluidAmount() {
        FluidStack fluid = getFluid();
        return fluid != null ? fluid.amount : 0;
    }

    @Override
    public int getCapacity() {
        return drawer.getMaxCapacity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

}
