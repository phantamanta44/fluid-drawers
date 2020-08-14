package xyz.phanta.fluiddrawers.util;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class FluidTypeMultimap<T> {

    private final FluidTypeMap<Collection<T>> backing = new FluidTypeMap<>();
    private final Supplier<? extends Collection<T>> collectionFactory;

    public FluidTypeMultimap(Supplier<? extends Collection<T>> collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    public FluidTypeMultimap() {
        this(ArrayList::new);
    }

    public void put(@Nullable FluidStack fluid, T value) {
        backing.getOrPut(fluid, collectionFactory).add(value);
    }

    public Collection<T> get(@Nullable FluidStack fluid) {
        Collection<T> result = backing.get(fluid);
        return result != null ? result : Collections.emptyList();
    }

    public void clear() {
        backing.clear();
    }

    public void forEach(FluidTypeMap.Visitor<Collection<T>> visitor) {
        backing.forEach(visitor);
    }

    public void forEachBinding(FluidTypeMap.Visitor<T> visitor) {
        backing.forEach((fluid, collection) -> {
            for (T value : collection) {
                visitor.visit(fluid, value);
            }
        });
    }

}
