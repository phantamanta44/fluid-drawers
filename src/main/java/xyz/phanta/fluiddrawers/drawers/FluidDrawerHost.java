package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public interface FluidDrawerHost {

    FluidDrawerGroup getFluidDrawerGroup();

    int getUnmodifiedBaseCapacity();

    int getModifiedBaseCapacity();

    int getStorageMultiplier();

    default int getCapacity() {
        IDrawerAttributes attrs = getAttributes();
        return attrs.isUnlimitedStorage() || attrs.isUnlimitedVending() ? Integer.MAX_VALUE
                : getModifiedBaseCapacity() * getStorageMultiplier();
    }

    IDrawerAttributes getAttributes();

    default boolean isFluidDrawerHostValid() {
        return true;
    }

    default void onStoredFluidChanged(int slot, @Nullable FluidStack oldFluid, @Nullable FluidStack newFluid) {
        // NO-OP
    }

}
