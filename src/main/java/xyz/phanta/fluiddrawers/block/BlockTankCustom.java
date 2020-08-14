package xyz.phanta.fluiddrawers.block;

import io.github.phantamanta44.libnine.item.L9ItemBlock;
import io.github.phantamanta44.libnine.util.collection.Accrue;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import xyz.phanta.fluiddrawers.block.base.BlockTankBase;
import xyz.phanta.fluiddrawers.client.util.FramedModelData;
import xyz.phanta.fluiddrawers.constant.NameConst;
import xyz.phanta.fluiddrawers.item.block.ItemBlockTankCustom;
import xyz.phanta.fluiddrawers.tile.TileTank;
import xyz.phanta.fluiddrawers.tile.TileTankCustom;
import xyz.phanta.fluiddrawers.tile.base.FramedTile;

import javax.annotation.Nullable;

public class BlockTankCustom extends BlockTankBase {

    public BlockTankCustom() {
        super(NameConst.BLOCK_TANK_CUSTOM, Material.WOOD);
        setSoundType(SoundType.WOOD);
        setTileFactory((w, m) -> new TileTankCustom());
    }

    @Override
    protected void accrueExtendedProperties(Accrue<IUnlistedProperty<?>> props) {
        props.accept(FramedModelData.PROP);
    }

    @Override
    protected L9ItemBlock initItemBlock() {
        return new ItemBlockTankCustom(this);
    }

    @Override
    protected ItemStack getDroppedDrawerItem(@Nullable TileTank tile) {
        ItemStack stack = super.getDroppedDrawerItem(tile);
        if (tile instanceof FramedTile) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            ((FramedTile)tile).getMaterialData().writeToNBT(tag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if (state instanceof IExtendedBlockState) {
            TileTankCustom tile = getTileEntity(world, pos);
            if (tile != null) {
                return ((IExtendedBlockState)state).withProperty(FramedModelData.PROP, new FramedModelData(tile));
            }
        }
        return state;
    }

}
