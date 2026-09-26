package xyz.phanta.fluiddrawers.item.block;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;
import io.github.phantamanta44.libnine.client.model.ParameterizedItemModel;
import io.github.phantamanta44.libnine.item.L9ItemBlockStated;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import xyz.phanta.fluiddrawers.FluidDrawersConfig;
import xyz.phanta.fluiddrawers.block.base.BlockTankBase;
import xyz.phanta.fluiddrawers.constant.NameConst;
import xyz.phanta.fluiddrawers.tile.TileTank;
import xyz.phanta.fluiddrawers.util.SimpleDrawerAttributes;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockTank extends L9ItemBlockStated implements ParameterizedItemModel.IParamaterized {

    public ItemBlockTank(BlockTankBase block) {
        super(block);
    }

    @Override
    public void getModelMutations(ItemStack stack, ParameterizedItemModel.Mutation m) {
        NBTTagCompound tag = stack.getTagCompound();
        m.mutate("sealed", (tag != null && tag.hasKey("Tile", Constants.NBT.TAG_COMPOUND)) ? "true" : "false");
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            return false;
        }
        TileEntity tileUnchecked = world.getTileEntity(pos);
        if (tileUnchecked instanceof TileTank) {
            TileTank tile = (TileTank)tileUnchecked;
            NBTTagCompound stackTag = stack.getTagCompound();
            if (stackTag != null && stackTag.hasKey("Tile", Constants.NBT.TAG_COMPOUND)) {
                tile.readFromPortableNBT(stackTag.getCompoundTag("Tile"));
            }
            tile.setIsSealed(false);
            if (StorageDrawers.config.cache.defaultQuantify) {
                IDrawerAttributes attrs = tile.getAttributes();
                if (attrs instanceof IDrawerAttributesModifiable) {
                    ((IDrawerAttributesModifiable)attrs).setIsShowingQuantity(true);
                }
            }
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flags) {
        super.addInformation(stack, world, tooltip, flags);

        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null || !stackTag.hasKey("Tile", Constants.NBT.TAG_COMPOUND)) {
            tooltip.add(I18n.format(NameConst.INFO_TANK_CAPACITY, FluidDrawersConfig.baseCapacity));
            return;
        }

        if (!FluidDrawersConfig.showSealedDrawerContents) {
            tooltip.add(TextFormatting.YELLOW + I18n.format("storagedrawers.drawers.sealed"));
            return;
        }

        NBTTagCompound tileTag = stackTag.getCompoundTag("Tile");
        SimpleDrawerAttributes attributes = new SimpleDrawerAttributes();
        UpgradeData upgrades = new UpgradeData(7);
        upgrades.setDrawerAttributes(attributes);
        upgrades.readFromNBT(tileTag);

        FluidStack fluid = FluidStack.loadFluidStackFromNBT(tileTag.getCompoundTag("Drawer").getCompoundTag("Fluid"));
        if (fluid == null || fluid.amount <= 0) {
            tooltip.add(I18n.format(NameConst.INFO_TANK_CAPACITY, getSealedCapacity(attributes, upgrades)));
            return;
        }

        tooltip.add(fluid.getLocalizedName());
        if (attributes.isUnlimitedVending()) {
            tooltip.add(I18n.format(NameConst.INFO_INFINITE));
        } else {
            tooltip.add(I18n.format(NameConst.INFO_TANK_CONTENTS, fluid.amount, getSealedCapacity(attributes, upgrades)));
        }
    }

    private int getSealedCapacity(IDrawerAttributes attributes, UpgradeData upgrades) {
        return attributes.isUnlimitedStorage()
                ? Integer.MAX_VALUE
                : (upgrades.hasOneStackUpgrade()
                ? FluidDrawersConfig.baseCapacityDowngraded
                : FluidDrawersConfig.baseCapacity) * upgrades.getStorageMultiplier();
    }
}
