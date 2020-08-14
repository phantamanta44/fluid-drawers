package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.storagedrawers.api.storage.EmptyDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import com.jaquadro.minecraft.storagedrawers.capabilities.CapabilityDrawerAttributes;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import xyz.phanta.fluiddrawers.util.DrawerReflect;
import xyz.phanta.fluiddrawers.util.FluidTypeMultimap;

import javax.annotation.Nullable;
import java.util.*;

public class FluidDrawerController implements IFluidHandler, ICapabilityProvider {

    private final TileEntityController tile;
    private final List<FluidSlotRecord> fluidSlots = new ArrayList<>();
    private final FluidTypeMultimap<FluidSlotRecord> fluidLookupCache = new FluidTypeMultimap<>();

    @Nullable
    private Map<BlockPos, ?> drawerRecords = null;

    public FluidDrawerController(TileEntityController tile) {
        this.tile = tile;
    }

    public void rebuildCache() {
        fluidSlots.clear();
        fluidLookupCache.clear();
        if (drawerRecords == null) {
            drawerRecords = DrawerReflect.getStorageRecords(tile);
        }
        drawerRecords.forEach((pos, record) -> {
            IDrawerGroup group = DrawerReflect.getGroupForRecord(record);
            if (group instanceof FluidDrawerGroup) {
                FluidDrawerGroup fluidGroup = (FluidDrawerGroup)group;
                if (fluidGroup.isFluidDrawerGroupValid()) {
                    for (int i = 0; i < fluidGroup.getFluidDrawerCount(); i++) {
                        fluidSlots.add(new FluidSlotRecord(fluidGroup, i, pos));
                    }
                }
            }
        });
        Collections.sort(fluidSlots);
        for (int i = 0; i < fluidSlots.size(); i++) {
            FluidSlotRecord record = fluidSlots.get(i);
            record.index = i;
            FluidStack fluid = record.getDrawer().getStoredFluid();
            if (fluid != null) {
                fluidLookupCache.put(fluid, record);
            }
        }
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return fluidSlots.stream()
                .filter(FluidSlotRecord::isDrawerValid)
                .map(record -> {
                    FluidDrawer drawer = record.getDrawer();
                    return new FluidTankProperties(drawer.getStoredFluid(), drawer.getMaxCapacity(), true, true);
                })
                .toArray(IFluidTankProperties[]::new);
    }

    @Override
    public int fill(FluidStack fluid, boolean commit) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }
        int origAmount = fluid.amount;
        fluid = fluid.copy();
        IntSet seen = new IntOpenHashSet();
        for (FluidSlotRecord record : fluidLookupCache.get(fluid)) {
            if (record.isDrawerValid()) {
                fluid.amount -= record.getDrawer().insertFluid(fluid, commit, false);
                if (fluid.amount <= 0) {
                    return origAmount;
                }
            }
            seen.add(record.index);
        }
        for (FluidSlotRecord record : fluidSlots) {
            if (!seen.contains(record.index) && record.isDrawerValid()) {
                int transferred = record.getDrawer().insertFluid(fluid, commit, false);
                if (transferred > 0) {
                    fluidLookupCache.put(fluid, record);
                    fluid.amount -= transferred;
                    if (fluid.amount <= 0) {
                        return origAmount;
                    }
                }
            }
        }
        return origAmount - fluid.amount;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluid, boolean commit) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        int origAmount = fluid.amount;
        fluid = fluid.copy();
        IntSet seen = new IntOpenHashSet();
        for (FluidSlotRecord record : fluidLookupCache.get(fluid)) {
            if (record.isDrawerValid()) {
                FluidStack transferred = record.getDrawer().extractFluid(fluid, commit, false);
                if (transferred != null && transferred.amount > 0) {
                    fluid.amount -= transferred.amount;
                    if (fluid.amount <= 0) {
                        fluid.amount = origAmount;
                        return fluid;
                    }
                }
            }
            seen.add(record.index);
        }
        for (FluidSlotRecord record : fluidSlots) {
            if (!seen.contains(record.index) && record.isDrawerValid()) {
                FluidStack transferred = record.getDrawer().extractFluid(fluid, commit, false);
                if (transferred != null && transferred.amount > 0) {
                    fluidLookupCache.put(fluid, record);
                    fluid.amount -= transferred.amount;
                    if (fluid.amount <= 0) {
                        fluid.amount = origAmount;
                        return fluid;
                    }
                }
            }
        }
        fluid.amount = origAmount - fluid.amount;
        return fluid;
    }

    @Nullable
    @Override
    public FluidStack drain(int amount, boolean commit) {
        FluidStack fluid = null;
        for (FluidSlotRecord record : fluidSlots) {
            if (record.isDrawerValid()) {
                fluid = record.getDrawer().extractFluid(amount, false, false);
                if (fluid != null && fluid.amount > 0) {
                    break;
                }
            }
        }
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }
        fluid.amount = amount;
        return drain(fluid, commit);
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

    private static class FluidSlotRecord implements Comparable<FluidSlotRecord> {

        private static final IDrawerAttributes EMPTY_ATTRS = new EmptyDrawerAttributes();
        private static final int PRI_LOCKED = 0;
        private static final int PRI_LOCKED_VOID = 1;
        private static final int PRI_NORMAL = 2;
        private static final int PRI_VOID = 3;
        private static final int PRI_EMPTY = 4;
        private static final int PRI_LOCKED_EMPTY = 5;

        private final FluidDrawerGroup group;
        private final int slot;
        private final BlockPos pos;
        private final int priority;

        int index;

        FluidSlotRecord(FluidDrawerGroup group, int slot, BlockPos pos) {
            this.group = group;
            this.slot = slot;
            this.pos = pos;
            this.priority = computePriority();
        }

        int computePriority() {
            FluidDrawer drawer = group.getFluidDrawer(slot);
            IDrawerAttributes attrs = group.hasCapability(CapabilityDrawerAttributes.DRAWER_ATTRIBUTES_CAPABILITY, null)
                    ? Objects.requireNonNull(group.getCapability(CapabilityDrawerAttributes.DRAWER_ATTRIBUTES_CAPABILITY, null))
                    : EMPTY_ATTRS;
            if (drawer.isEmpty()) {
                return attrs.isItemLocked(LockAttribute.LOCK_EMPTY) ? PRI_LOCKED_EMPTY : PRI_EMPTY;
            } else if (attrs.isVoid()) {
                return attrs.isItemLocked(LockAttribute.LOCK_POPULATED) ? PRI_LOCKED_VOID : PRI_VOID;
            } else {
                return attrs.isItemLocked(LockAttribute.LOCK_POPULATED) ? PRI_LOCKED : PRI_NORMAL;
            }
        }

        FluidDrawer getDrawer() {
            return group.getFluidDrawer(slot);
        }

        boolean isDrawerValid() {
            return group.isFluidDrawerGroupValid();
        }

        @Override
        public int compareTo(FluidSlotRecord other) {
            int diff = priority - other.priority;
            return diff != 0 ? diff : pos.compareTo(other.pos);
        }

    }

}
