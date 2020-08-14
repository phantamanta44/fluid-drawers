package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import io.github.phantamanta44.libnine.util.math.MathUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class SimpleFluidDrawer implements FluidDrawer, INBTSerializable<NBTTagCompound> {

    private final int slot;
    private final FluidDrawerHost host;

    @Nullable
    private FluidStack fluid = null;

    public SimpleFluidDrawer(int slot, FluidDrawerHost host) {
        this.slot = slot;
        this.host = host;
    }

    @Nullable
    @Override
    public FluidStack getStoredFluid() {
        if (fluid != null && host.getAttributes().isUnlimitedVending()) {
            fluid.amount = Integer.MAX_VALUE;
        }
        return fluid;
    }

    @Override
    public FluidDrawer setStoredFluid(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            if (this.fluid != null && this.fluid.amount > 0) {
                FluidStack oldFluid;
                if (host.getAttributes().isItemLocked(LockAttribute.LOCK_POPULATED)) {
                    oldFluid = this.fluid.copy();
                    this.fluid.amount = 0;
                } else {
                    oldFluid = this.fluid;
                    this.fluid = null;
                }
                host.onStoredFluidChanged(slot, oldFluid, this.fluid);
            }
        } else if (host.getAttributes().isUnlimitedVending()) {
            if (this.fluid == null || !fluid.isFluidEqual(this.fluid)) {
                updateFluid(fluid, Integer.MAX_VALUE);
            }
        } else if (this.fluid == null || !fluid.isFluidEqual(this.fluid)) {
            updateFluid(fluid, Math.min(fluid.amount, getMaxCapacity()));
        } else {
            FluidStack oldFluid = this.fluid.copy();
            this.fluid.amount = MathUtils.clamp(fluid.amount, 0, getMaxCapacity());
            host.onStoredFluidChanged(slot, oldFluid, this.fluid);
        }
        return this;
    }

    private void updateFluid(FluidStack newFluid, int amount) {
        FluidStack oldFluid = fluid;
        fluid = newFluid.copy();
        fluid.amount = MathUtils.clamp(amount, 0, getMaxCapacity());
        host.onStoredFluidChanged(slot, oldFluid, fluid);
    }

    @Override
    public int getMaxCapacity(@Nullable FluidStack fluid) {
        return host.getCapacity();
    }

    @Override
    public int getAcceptingMaxCapacity(@Nullable FluidStack fluid) {
        return (host.getAttributes().isVoid() || host.getAttributes().isUnlimitedVending()) ? Integer.MAX_VALUE
                : getMaxCapacity(fluid);
    }

    @Override
    public int getRemainingCapacity() {
        FluidStack fluid = getStoredFluid();
        return fluid != null ? (getMaxCapacity() - fluid.amount) : getMaxCapacity();
    }

    @Override
    public int getAcceptingRemainingCapacity() {
        return host.getAttributes().isVoid() || host.getAttributes().isUnlimitedVending() ? Integer.MAX_VALUE
                : getRemainingCapacity();
    }

    @Override
    public boolean canFluidBeStored(@Nullable FluidStack fluid) {
        if (fluid == null) {
            return true;
        }
        return this.fluid == null ? !host.getAttributes().isItemLocked(LockAttribute.LOCK_EMPTY)
                : (this.fluid.amount == 0 && !host.getAttributes().isItemLocked(LockAttribute.LOCK_POPULATED))
                || this.fluid.isFluidEqual(fluid);
    }

    @Override
    public boolean canFluidBeExtracted(@Nullable FluidStack fluid) {
        return fluid == null || (this.fluid != null && this.fluid.isFluidEqual(fluid));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (fluid != null) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluid.writeToNBT(fluidTag);
            tag.setTag("Fluid", fluidTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Fluid", Constants.NBT.TAG_COMPOUND)) {
            fluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("Fluid"));
        }
    }

}
