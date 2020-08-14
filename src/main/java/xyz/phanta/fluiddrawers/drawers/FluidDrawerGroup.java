package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;

public interface FluidDrawerGroup extends IDrawerGroup {

    int[] NO_DRAWER_SLOTS = new int[0];

    int getFluidDrawerCount();

    FluidDrawer getFluidDrawer(int slot);

    int[] getAccessibleFluidDrawerSlots();

    default boolean isFluidDrawerGroupValid() {
        return true;
    }

    @Override
    default int getDrawerCount() {
        return 0;
    }

    @Override
    default IDrawer getDrawer(int slot) {
        throw new UnsupportedOperationException("Fluid drawer group has no item drawers!");
    }

    @Override
    default int[] getAccessibleDrawerSlots() {
        return NO_DRAWER_SLOTS;
    }

}
