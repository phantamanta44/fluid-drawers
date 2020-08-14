package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.chameleon.block.tiledata.TileDataShim;
import com.jaquadro.minecraft.storagedrawers.capabilities.CapabilityDrawerAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import xyz.phanta.fluiddrawers.util.DrawerFluidHandler;

import javax.annotation.Nullable;

public class SingletonFluidDrawerGroup extends TileDataShim implements FluidDrawerGroup {

    private static final int[] SINGLETON_SLOT = new int[] { 0 };

    private final FluidDrawerHost host;
    private final SimpleFluidDrawer fluidDrawer;
    private final IFluidHandler fluidHandler;

    public SingletonFluidDrawerGroup(FluidDrawerHost host) {
        this.host = host;
        this.fluidDrawer = new SimpleFluidDrawer(0, host);
        this.fluidHandler = new DrawerFluidHandler(this);
    }

    public FluidDrawer getFluidDrawer() {
        return fluidDrawer;
    }

    @Override
    public int getFluidDrawerCount() {
        return 1;
    }

    @Override
    public FluidDrawer getFluidDrawer(int slot) {
        if (slot != 0) {
            throw new IndexOutOfBoundsException("Singleton fluid drawer group only has slot 0, but got: " + slot);
        }
        return getFluidDrawer();
    }

    @Override
    public int[] getAccessibleFluidDrawerSlots() {
        return SINGLETON_SLOT;
    }

    @Override
    public boolean isFluidDrawerGroupValid() {
        return host.isFluidDrawerHostValid();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || capability == CapabilityDrawerAttributes.DRAWER_ATTRIBUTES_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        } else if (capability == CapabilityDrawerAttributes.DRAWER_ATTRIBUTES_CAPABILITY) {
            return CapabilityDrawerAttributes.DRAWER_ATTRIBUTES_CAPABILITY.cast(host.getAttributes());
        }
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("Drawer", fluidDrawer.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("Drawer", Constants.NBT.TAG_COMPOUND)) {
            fluidDrawer.deserializeNBT(tag.getCompoundTag("Drawer"));
        }
    }

}
