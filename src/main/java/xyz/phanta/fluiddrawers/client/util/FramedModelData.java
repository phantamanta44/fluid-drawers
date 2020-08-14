package xyz.phanta.fluiddrawers.client.util;

import com.jaquadro.minecraft.chameleon.block.properties.UnlistedModelData;
import com.jaquadro.minecraft.chameleon.model.ModelData;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.MaterialData;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.property.IUnlistedProperty;
import xyz.phanta.fluiddrawers.tile.base.FramedTile;

// every constructor of FramedModelData is useless so we're stuck with this instead
public class FramedModelData extends ModelData {

    public static final IUnlistedProperty<FramedModelData> PROP = UnlistedModelData.create(FramedModelData.class);

    public static final FramedModelData EMPTY = new FramedModelData();

    private final ItemStack matFront, matSide, matTrim;
    private final ItemStack effectiveMatFront, effectiveMatSide, effectiveMatTrim;

    public FramedModelData(ItemStack matFront, ItemStack matSide, ItemStack matTrim,
                           ItemStack effectiveMatFront, ItemStack effectiveMatSide, ItemStack effectiveMatTrim) {
        this.matFront = matFront;
        this.matSide = matSide;
        this.matTrim = matTrim;
        this.effectiveMatFront = effectiveMatFront;
        this.effectiveMatSide = effectiveMatSide;
        this.effectiveMatTrim = effectiveMatTrim;
    }

    public FramedModelData(MaterialData materialData) {
        this(materialData.getFront(), materialData.getSide(), materialData.getTrim(),
                materialData.getEffectiveFront(), materialData.getEffectiveSide(), materialData.getEffectiveTrim());
    }

    public FramedModelData(FramedTile tile) {
        this(tile.getMaterialData());
    }

    private FramedModelData() {
        this(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public ItemStack getEffectiveFrontMaterial() {
        return effectiveMatFront;
    }

    public ItemStack getEffectiveSideMaterial() {
        return effectiveMatSide;
    }

    public ItemStack getEffectiveTrimMaterial() {
        return effectiveMatTrim;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FramedModelData)) {
            return false;
        }
        FramedModelData other = (FramedModelData)obj;
        return ItemStack.areItemsEqual(matFront, other.matFront)
                && ItemStack.areItemsEqual(matSide, other.matSide)
                && ItemStack.areItemsEqual(matTrim, other.matTrim);
    }

    @Override
    public int hashCode() {
        int c = matFront.getItem().hashCode();
        c = 37 * c + matFront.getItemDamage();
        c = 37 * c + matSide.getItem().hashCode();
        c = 37 * c + matSide.getItemDamage();
        c = 37 * c + matTrim.getItem().hashCode();
        c = 37 * c + matTrim.getItemDamage();
        return c;
    }

}
