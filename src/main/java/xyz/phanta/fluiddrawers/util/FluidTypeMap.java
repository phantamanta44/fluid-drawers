package xyz.phanta.fluiddrawers.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FluidTypeMap<T> {

    private final Map<Fluid, SubMap<T>> backing = new HashMap<>();
    @Nullable
    private T nullMapping = null;

    public void put(@Nullable FluidStack fluid, T value) {
        if (fluid == null) {
            nullMapping = value;
        } else {
            backing.computeIfAbsent(fluid.getFluid(), k -> new SubMap<>()).put(fluid.tag, value);
        }
    }

    @Nullable
    public T get(@Nullable FluidStack fluid) {
        if (fluid == null) {
            return nullMapping;
        } else {
            SubMap<T> subMap = backing.get(fluid.getFluid());
            return subMap != null ? subMap.get(fluid.tag) : null;
        }
    }

    public T getOrPut(@Nullable FluidStack fluid, Supplier<? extends T> valueFactory) {
        if (fluid == null) {
            if (nullMapping == null) {
                nullMapping = valueFactory.get();
            }
            return nullMapping;
        } else {
            return backing.computeIfAbsent(fluid.getFluid(), k -> new SubMap<>()).getOrPut(fluid.tag, valueFactory);
        }
    }

    public void clear() {
        backing.clear();
    }

    public void forEach(Visitor<T> visitor) {
        if (nullMapping != null) {
            visitor.visit(null, nullMapping);
        }
        backing.forEach((fluidType, subMap) -> subMap.forEach(fluidType, visitor));
    }

    @FunctionalInterface
    public interface Visitor<T> {

        void visit(@Nullable FluidStack fluid, T value);

    }

    private static class SubMap<T> {

        private final Map<NBTTagCompound, T> backing = new HashMap<>();
        @Nullable
        private T nullMapping = null;

        void put(@Nullable NBTTagCompound tag, T value) {
            if (tag == null) {
                nullMapping = value;
            } else {
                backing.put(tag, value);
            }
        }

        @Nullable
        T get(@Nullable NBTTagCompound tag) {
            return tag == null ? nullMapping : backing.get(tag);
        }

        T getOrPut(@Nullable NBTTagCompound tag, Supplier<? extends T> valueFactory) {
            if (tag == null) {
                if (nullMapping == null) {
                    nullMapping = valueFactory.get();
                }
                return nullMapping;
            } else {
                return backing.computeIfAbsent(tag, k -> valueFactory.get());
            }
        }

        void forEach(Fluid fluidType, Visitor<T> visitor) {
            if (nullMapping != null) {
                visitor.visit(new FluidStack(fluidType, Fluid.BUCKET_VOLUME), nullMapping);
            }
            backing.forEach((tag, value) -> visitor.visit(new FluidStack(fluidType, Fluid.BUCKET_VOLUME, tag), value));
        }

    }

}
