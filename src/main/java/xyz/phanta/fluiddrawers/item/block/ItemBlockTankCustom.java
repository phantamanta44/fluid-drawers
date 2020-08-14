package xyz.phanta.fluiddrawers.item.block;

import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.MaterialData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.phanta.fluiddrawers.block.BlockTankCustom;
import xyz.phanta.fluiddrawers.item.base.FramedItem;
import xyz.phanta.fluiddrawers.tile.TileTankCustom;

public class ItemBlockTankCustom extends ItemBlockTank implements FramedItem {

    public ItemBlockTankCustom(BlockTankCustom block) {
        super(block);
    }

    @Override
    public MaterialData getMaterialData(ItemStack stack) {
        MaterialData materialData = new MaterialData();
        if (stack.hasTagCompound()) {
            materialData.readFromNBT(stack.getTagCompound());
        }
        return materialData;
    }

    @Override
    public ItemStack createFramedStack(ItemStack stack, ItemStack matSide, ItemStack matTrim, ItemStack matFront) {
        MaterialData materialData = new MaterialData();
        materialData.setSide(matSide);
        materialData.setTrim(matTrim);
        materialData.setFront(matFront);
        ItemStack newStack = stack.copy();
        NBTTagCompound tag = newStack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        materialData.writeToNBT(tag);
        newStack.setTagCompound(tag);
        return newStack;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            return false;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTankCustom) {
            MaterialData tileMatData = ((TileTankCustom)tile).getMaterialData();
            MaterialData itemMatData = getMaterialData(stack);
            tileMatData.setFront(itemMatData.getFront());
            tileMatData.setSide(itemMatData.getSide());
            tileMatData.setTrim(itemMatData.getTrim());
        }
        return true;
    }

}
