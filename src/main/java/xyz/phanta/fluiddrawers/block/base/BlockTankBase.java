package xyz.phanta.fluiddrawers.block.base;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.INetworked;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.*;
import com.jaquadro.minecraft.storagedrawers.security.SecurityManager;
import io.github.phantamanta44.libnine.block.L9BlockStated;
import io.github.phantamanta44.libnine.util.collection.Accrue;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.drawers.FluidDrawerGroup;
import xyz.phanta.fluiddrawers.init.FdGuis;
import xyz.phanta.fluiddrawers.tile.TileTank;
import xyz.phanta.fluiddrawers.util.BlockInteractionUtils;

import javax.annotation.Nullable;

public abstract class BlockTankBase extends L9BlockStated implements INetworked {

    private static final PropertyBool LOCKED = PropertyBool.create("locked");
    private static final PropertyBool VOIDING = PropertyBool.create("voiding");
    private static final PropertyBool SEALED = PropertyBool.create("sealed");
    private static final PropertyBool SECURE = PropertyBool.create("secure");
    private static final PropertyBool VENDING = PropertyBool.create("vending");

    public BlockTankBase(String name, Material material) {
        super(name, material);
        setHardness(5F);
    }

    @Override
    protected void accrueVolatileProperties(Accrue<IProperty<?>> props) {
        props.acceptAll(LOCKED, VOIDING, SEALED, SECURE, VENDING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileTank tile = getTileEntity(world, pos);
        if (tile == null) {
            return super.getActualState(state, world, pos);
        }
        return super.getActualState(state, world, pos)
                .withProperty(LOCKED, tile.getAttributes().isItemLocked(LockAttribute.LOCK_POPULATED))
                .withProperty(VOIDING, tile.getAttributes().isVoid())
                .withProperty(SEALED, tile.isSealed())
                .withProperty(SECURE, tile.getOwner() != null)
                .withProperty(VENDING, tile.getAttributes().isUnlimitedVending());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileTank tile = getTileEntity(world, pos);
        if (tile != null) {
            if (stack.hasDisplayName()) {
                tile.setDisplayName(stack.getDisplayName());
            }
            if (placer.getHeldItemOffhand().getItem() == ModItems.drawerKey) {
                tile.setLockState(LockAttribute.LOCK_EMPTY, LockAttribute.LOCK_POPULATED);
            }
            tile.markDirty();
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileTank tile = getTileEntity(world, pos);
        if (tile == null) {
            return false;
        }
        if (hand == EnumHand.OFF_HAND || !SecurityManager.hasAccess(player.getGameProfile(), tile)) {
            return false;
        }
        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty()) {
            if (player.isSneaking()) {
                if (tile.isSealed()) {
                    tile.setIsSealed(false);
                } else if (StorageDrawers.config.cache.enableDrawerUI) {
                    FluidDrawersMod.INSTANCE.getGuiHandler().openGui(player, FdGuis.TANK, new WorldBlockPos(world, pos));
                }
                return true;
            }
            return false;
        }
        Item heldItem = held.getItem();
        if (heldItem instanceof ItemKey || heldItem == ModItems.tape) {
            return false;
        } else if (heldItem instanceof ItemUpgrade) {
            UpgradeData upgrades = tile.getUpgrades();
            if (!upgrades.canAddUpgrade(held)) {
                if (!world.isRemote) {
                    player.sendStatusMessage(new TextComponentTranslation("storagedrawers.msg.cannotAddUpgrade"), true);
                }
                return false;
            }
            if (!upgrades.addUpgrade(held)) {
                if (!world.isRemote) {
                    player.sendStatusMessage(new TextComponentTranslation("storagedrawers.msg.maxUpgrades"), true);
                }
                return false;
            }
            world.notifyBlockUpdate(pos, state, state, 3);
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
                if (held.getCount() <= 0) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                }
            }
            return true;
        } else if (heldItem instanceof ItemPersonalKey) {
            String securityKey = ((ItemPersonalKey)heldItem).getSecurityProviderKey(held.getItemDamage());
            if (tile.getOwner() == null) {
                tile.setOwner(player.getPersistentID());
                tile.setSecurityProvider(StorageDrawers.securityRegistry.getProvider(securityKey));
            } else if (SecurityManager.hasOwnership(player.getGameProfile(), tile)) {
                tile.setOwner(null);
                tile.setSecurityProvider(null);
            } else {
                return false;
            }
            return true;
        } else if (facing.getAxis() != EnumFacing.Axis.Y && !tile.isSealed()) {
            BlockInteractionUtils.transferFluid(tile, player, hand, facing, true);
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileTank tile = getTileEntity(world, pos);
        if (tile == null) {
            super.breakBlock(world, pos, state);
            return;
        }
        FluidStack spilledFluid = null;
        if (!tile.isSealed() && !StorageDrawers.config.cache.keepContentsOnBreak) {
            UpgradeData upgrades = tile.getUpgrades();
            for (int i = 0; i < upgrades.getSlotCount(); i++) {
                ItemStack stack = upgrades.getUpgrade(i);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof ItemUpgradeCreative) {
                        continue; // see ya!
                    }
                    spawnAsEntity(world, pos, stack);
                }
            }
            if (!tile.getAttributes().isUnlimitedVending()) {
                FluidDrawerGroup drawerGroup = tile.getFluidDrawerGroup(); // more generic than necessary
                for (int i = 0; i < drawerGroup.getFluidDrawerCount(); i++) {
                    FluidDrawer drawer = drawerGroup.getFluidDrawer(i);
                    FluidStack stored = drawer.getStoredFluid();
                    if (stored != null && stored.amount > 0 && stored.getFluid().canBePlacedInWorld()
                            && (spilledFluid == null || stored.amount > spilledFluid.amount)) {
                        spilledFluid = stored;
                    }
                }
            }
        }
        super.breakBlock(world, pos, state);
        if (spilledFluid != null) {
            Fluid spilledType = spilledFluid.getFluid();
            if (world.provider.doesWaterVaporize() && spilledType.doesVaporize(spilledFluid)) {
                spilledType.vaporize(null, world, pos, spilledFluid);
            } else {
                Block spilledBlock = spilledType.getBlock();
                if (spilledBlock instanceof IFluidBlock) {
                    ((IFluidBlock)spilledBlock).place(world, pos, spilledFluid, true);
                } else if (spilledBlock instanceof BlockLiquid) {
                    if (spilledFluid.amount >= 1000) {
                        world.setBlockState(pos,
                                spilledBlock.getDefaultState().withProperty(BlockLiquid.LEVEL, 0),
                                Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    } else if (spilledFluid.amount >= 750) {
                        world.setBlockState(pos,
                                spilledBlock.getDefaultState().withProperty(BlockLiquid.LEVEL, 15),
                                Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    } else if (spilledFluid.amount >= 500) {
                        world.setBlockState(pos,
                                spilledBlock.getDefaultState().withProperty(BlockLiquid.LEVEL, 11),
                                Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    } else if (spilledFluid.amount >= 250) {
                        world.setBlockState(pos,
                                spilledBlock.getDefaultState().withProperty(BlockLiquid.LEVEL, 7),
                                Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    } else {
                        world.setBlockState(pos,
                                spilledBlock.getDefaultState().withProperty(BlockLiquid.LEVEL, 3),
                                Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    }
                    world.neighborChanged(pos, spilledBlock, pos);
                } else if (spilledFluid.amount >= 1000) {
                    world.setBlockState(pos, spilledBlock.getDefaultState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    world.neighborChanged(pos, spilledBlock, pos);
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        // prevents the block from being destroyed if being harvested and thus allows the tile entity to remain for getDrops
        // the block is eventually destroyed later in harvestBlock
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        // have to destroy the block now in case we prevented it from being destroyed earlier in removedByPlayer
        world.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(getDroppedDrawerItem(getTileEntity(world, pos)));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return getDroppedDrawerItem(getTileEntity(world, pos));
    }

    protected ItemStack getDroppedDrawerItem(@Nullable TileTank tile) {
        ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1, 0);
        if (tile != null) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            boolean hasContents = false;
            if (StorageDrawers.config.cache.keepContentsOnBreak) {
                find_contents:
                {
                    FluidDrawerGroup drawerGroup = tile.getFluidDrawerGroup(); // more generic than necessary
                    for (int i = 0; i < drawerGroup.getFluidDrawerCount(); i++) {
                        if (!drawerGroup.getFluidDrawer(i).isEmpty()) {
                            hasContents = true;
                            break find_contents;
                        }
                    }
                    UpgradeData upgrades = tile.getUpgrades();
                    for (int i = 0; i < upgrades.getSlotCount(); i++) {
                        if (!upgrades.getUpgrade(i).isEmpty()) {
                            hasContents = true;
                            break find_contents;
                        }
                    }
                }
            }
            if (tile.isSealed() || (StorageDrawers.config.cache.keepContentsOnBreak && hasContents)) {
                NBTTagCompound tiledata = new NBTTagCompound();
                tile.writeToNBT(tiledata);
                tag.setTag("Tile", tiledata);
            }
            if (tile.hasCustomName()) {
                stack.setStackDisplayName(tile.getName());
            }
            if (!tag.isEmpty()) {
                stack.setTagCompound(tag);
            }
        }
        return stack;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileTank tile = getTileEntity(world, pos);
        if (tile != null) {
            UpgradeData upgrades = tile.getUpgrades();
            for (int i = 0; i < upgrades.getSlotCount(); i++) {
                ItemStack stack = upgrades.getUpgrade(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemUpgradeStorage
                        && EnumUpgradeStorage.byMetadata(stack.getMetadata()) == EnumUpgradeStorage.OBSIDIAN) {
                    return 1000;
                }
            }
        }
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileTank tile = getTileEntity(world, pos);
        return (tile != null && tile.hasLevelEmitter()) ? tile.getRedstoneLevel() : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP ? getWeakPower(state, worldIn, pos, side) : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

}
