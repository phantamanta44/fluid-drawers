package xyz.phanta.fluiddrawers.item.base;

import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.MaterialData;
import net.minecraft.item.ItemStack;

public interface FramedItem {

    MaterialData getMaterialData(ItemStack stack);

    ItemStack createFramedStack(ItemStack stack, ItemStack matSide, ItemStack matTrim, ItemStack matFront);

}
