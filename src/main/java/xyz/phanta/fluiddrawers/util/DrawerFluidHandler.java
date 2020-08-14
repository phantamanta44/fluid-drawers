package xyz.phanta.fluiddrawers.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.drawers.FluidDrawerGroup;

import javax.annotation.Nullable;

public class DrawerFluidHandler implements BypassableFluidHandler {

    private final FluidDrawerGroup group;

    public DrawerFluidHandler(FluidDrawerGroup group) {
        this.group = group;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        int[] slots = group.getAccessibleFluidDrawerSlots();
        IFluidTankProperties[] tankProps = new IFluidTankProperties[slots.length];
        for (int i = 0; i < slots.length; i++) {
            FluidDrawer drawer = group.getFluidDrawer(slots[i]);
            FluidStack fluid = drawer.getStoredFluid();
            tankProps[i] = new FluidTankProperties(fluid, drawer.getAcceptingMaxCapacity(fluid), true, true);
        }
        return tankProps;
    }

    @Override
    public int fill(FluidStack fluid, boolean commit, boolean bypass) {
        int origAmount = fluid.amount;
        fluid = fluid.copy();
        for (int slotIndex : group.getAccessibleFluidDrawerSlots()) {
            fluid.amount -= group.getFluidDrawer(slotIndex).insertFluid(fluid, commit, bypass);
            if (fluid.amount <= 0) {
                return origAmount;
            }
        }
        return origAmount - fluid.amount;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluid, boolean commit, boolean bypass) {
        int origAmount = fluid.amount;
        fluid = fluid.copy();
        for (int slotIndex : group.getAccessibleFluidDrawerSlots()) {
            FluidStack extracted = group.getFluidDrawer(slotIndex).extractFluid(fluid, commit, bypass);
            if (extracted != null && extracted.amount > 0) {
                fluid.amount -= extracted.amount;
                if (fluid.amount <= 0) {
                    fluid.amount = origAmount;
                    return fluid;
                }
            }
        }
        fluid.amount = origAmount - fluid.amount;
        return fluid;
    }

    @Nullable
    @Override
    public FluidStack drain(int amount, boolean commit, boolean bypass) {
        for (int slotIndex : group.getAccessibleFluidDrawerSlots()) {
            FluidStack fluid = group.getFluidDrawer(slotIndex).extractFluid(amount, false, bypass);
            if (fluid != null && fluid.amount > 0) {
                fluid.amount = amount;
                return drain(fluid, commit, bypass);
            }
        }
        return null;
    }

}
