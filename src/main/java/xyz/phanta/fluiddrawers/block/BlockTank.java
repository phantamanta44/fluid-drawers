package xyz.phanta.fluiddrawers.block;

import io.github.phantamanta44.libnine.item.L9ItemBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xyz.phanta.fluiddrawers.block.base.BlockTankBase;
import xyz.phanta.fluiddrawers.constant.NameConst;
import xyz.phanta.fluiddrawers.item.block.ItemBlockTank;
import xyz.phanta.fluiddrawers.tile.TileTank;
import xyz.phanta.fluiddrawers.tile.TileTankCustom;

import javax.annotation.Nullable;

public class BlockTank extends BlockTankBase {

    public BlockTank() {
        super(NameConst.BLOCK_TANK, Material.IRON);
        setSoundType(SoundType.METAL);
        setTileFactory((w, m) -> new TileTank());
    }

    @Override
    protected L9ItemBlock initItemBlock() {
        return new ItemBlockTank(this);
    }

    @Override
    protected ItemStack getDroppedDrawerItem(@Nullable TileTank tile) {
        ItemStack stack = super.getDroppedDrawerItem(tile);
        if (tile instanceof TileTankCustom) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            ((TileTankCustom)tile).getMaterialData().writeToNBT(tag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

}
