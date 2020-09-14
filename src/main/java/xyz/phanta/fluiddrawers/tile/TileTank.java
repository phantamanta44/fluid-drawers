package xyz.phanta.fluiddrawers.tile;

import com.jaquadro.minecraft.chameleon.block.ChamTileEntity;
import com.jaquadro.minecraft.chameleon.block.tiledata.CustomNameData;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.security.ISecurityProvider;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.IProtectable;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.ISealable;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.ControllerData;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;
import com.jaquadro.minecraft.storagedrawers.capabilities.CapabilityDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.EnumUpgradeStorage;
import io.github.phantamanta44.libnine.tile.RegisterTile;
import io.github.phantamanta44.libnine.util.math.MathUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import xyz.phanta.fluiddrawers.FluidDrawersConfig;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.constant.NameConst;
import xyz.phanta.fluiddrawers.drawers.DrawerUpgradable;
import xyz.phanta.fluiddrawers.drawers.FluidDrawer;
import xyz.phanta.fluiddrawers.drawers.FluidDrawerHost;
import xyz.phanta.fluiddrawers.drawers.SingletonFluidDrawerGroup;
import xyz.phanta.fluiddrawers.network.SPacketSyncFluidDrawerCount;
import xyz.phanta.fluiddrawers.network.SPacketSyncFluidDrawerFluid;
import xyz.phanta.fluiddrawers.util.SimpleDrawerAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@RegisterTile(FluidDrawersMod.MOD_ID)
public class TileTank extends ChamTileEntity implements FluidDrawerHost, DrawerUpgradable, ISealable, IProtectable, IWorldNameable {

    private final CustomNameData customNameData = new CustomNameData(NameConst.CONT_TANK);
    private final UpgradeData upgradeData = new TankUpgradeData();
    private final SingletonFluidDrawerGroup drawerData = new SingletonFluidDrawerGroup(this);

    // is this necessary? i can't find any obvious entry points that reach this field
    private final ControllerData controllerData = new ControllerData();

    private final TankAttributes attributes = new TankAttributes();

    private boolean sealed = false;
    @Nullable
    private UUID owner = null;
    @Nullable
    private String securityKey = null;

    public TileTank() {
        injectPortableData(customNameData);
        injectPortableData(upgradeData);
        injectPortableData(drawerData);
        injectData(controllerData);
        upgradeData.setDrawerAttributes(attributes);
    }

    @Override
    public SingletonFluidDrawerGroup getFluidDrawerGroup() {
        return drawerData;
    }

    @Override
    public UpgradeData getUpgrades() {
        return upgradeData;
    }

    @Override
    public int getUnmodifiedBaseCapacity() {
        return FluidDrawersConfig.baseCapacity;
    }

    private int getDowngradedBaseCapacity() {
        return FluidDrawersConfig.baseCapacityDowngraded;
    }

    @Override
    public int getModifiedBaseCapacity() {
        return upgradeData.hasOneStackUpgrade() ? getDowngradedBaseCapacity() : getUnmodifiedBaseCapacity();
    }

    @Override
    public int getStorageMultiplier() {
        return upgradeData.getStorageMultiplier();
    }

    @Override
    public IDrawerAttributes getAttributes() {
        return attributes;
    }

    public void setLockState(LockAttribute... locks) {
        EnumSet<LockAttribute> disabled = EnumSet.allOf(LockAttribute.class);
        for (LockAttribute lock : locks) {
            attributes.setItemLocked(lock, true);
            disabled.remove(lock);
        }
        for (LockAttribute lock : disabled) {
            attributes.setItemLocked(lock, false);
        }
    }

    @Nullable
    @Override
    public UUID getOwner() {
        if (!StorageDrawers.config.cache.enablePersonalUpgrades) {
            return null;
        }
        return owner;
    }

    @Override
    public boolean setOwner(UUID owner) {
        if (!StorageDrawers.config.cache.enablePersonalUpgrades) {
            return false;
        }
        if (!Objects.equals(this.owner, owner)) {
            this.owner = owner;
            notifyBlockUpdate();
        }
        return false;
    }

    @Override
    public ISecurityProvider getSecurityProvider() {
        return StorageDrawers.securityRegistry.getProvider(securityKey);
    }

    @Override
    public boolean setSecurityProvider(ISecurityProvider provider) {
        if (!StorageDrawers.config.cache.enablePersonalUpgrades) {
            return false;
        }
        String newKey = provider != null ? provider.getProviderID() : null;
        if (!Objects.equals(securityKey, newKey)) {
            securityKey = newKey;
            notifyBlockUpdate();
        }
        return true;
    }

    @Override
    public boolean isSealed() {
        if (!StorageDrawers.config.cache.enableTape) {
            return false;
        }
        return sealed;
    }

    @Override
    public boolean setIsSealed(boolean state) {
        if (!StorageDrawers.config.cache.enableTape) {
            return false;
        }
        if (sealed != state) {
            sealed = state;
            notifyBlockUpdate();
        }
        return true;
    }

    @Override
    public String getName() {
        return customNameData.getName();
    }

    @Override
    public boolean hasCustomName() {
        return customNameData.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return customNameData.getDisplayName();
    }

    public void setDisplayName(String name) {
        customNameData.setName(name);
        markDirty();
    }

    public boolean hasLevelEmitter() {
        return StorageDrawers.config.cache.enableRedstoneUpgrades && upgradeData.getRedstoneType() != null;
    }

    public int getRedstoneLevel() {
        if (upgradeData.getRedstoneType() == null) {
            return 0;
        }
        FluidDrawer drawer = drawerData.getFluidDrawer();
        FluidStack fluid = drawer.getStoredFluid();
        return (fluid == null || fluid.amount == 0) ? 0
                : MathUtils.clamp(1 + (int)Math.floor(14F * fluid.amount / (float)drawer.getMaxCapacity()), 1, 15);
    }

    @Override
    public boolean isFluidDrawerHostValid() {
        return !isInvalid() && hasWorld() && getWorld().getTileEntity(getPos()) == this;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityDrawerGroup.DRAWER_GROUP_CAPABILITY
                || drawerData.hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityDrawerGroup.DRAWER_GROUP_CAPABILITY) {
            return CapabilityDrawerGroup.DRAWER_GROUP_CAPABILITY.cast(drawerData);
        } else if (drawerData.hasCapability(capability, facing)) {
            return drawerData.getCapability(capability, facing);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    public void onStoredFluidChanged(int slot, @Nullable FluidStack oldFluid, @Nullable FluidStack newFluid) {
        if (hasWorld() && !getWorld().isRemote) {
            if (oldFluid == null) {
                if (newFluid != null) {
                    sendFluidUpdateToClients();
                }
            } else if (newFluid == null || !oldFluid.isFluidEqual(newFluid)) {
                sendFluidUpdateToClients();
            } else {
                sendCountUpdateToClients();
            }
            markDirty();
        }
    }

    private void sendFluidUpdateToClients() {
        BlockPos pos = getPos();
        FluidDrawersMod.INSTANCE.getNetworkHandler().sendToAllAround(
                new SPacketSyncFluidDrawerFluid(pos, drawerData.getFluidDrawer().getStoredFluid()),
                new NetworkRegistry.TargetPoint(getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
    }

    private void sendCountUpdateToClients() {
        BlockPos pos = getPos();
        FluidStack fluid = drawerData.getFluidDrawer().getStoredFluid();
        FluidDrawersMod.INSTANCE.getNetworkHandler().sendToAllAround(
                new SPacketSyncFluidDrawerCount(pos, fluid != null ? fluid.amount : 0),
                new NetworkRegistry.TargetPoint(getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
    }

    private void notifyBlockUpdate() {
        if (hasWorld()) {
            World world = getWorld();
            markDirty();
            if (!world.isRemote) {
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.DEFAULT);
            } else {
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    @Override
    public void markDirty() {
        if (hasLevelEmitter() && hasWorld()) {
            notifyNeighbors();
        }
        super.markDirty();
    }

    private void notifyNeighbors() {
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), false);
        getWorld().notifyNeighborsOfStateChange(getPos().down(), getBlockType(), false);
    }

    @Override
    public void notifyChanged() {
        markDirty();
    }

    @Override
    public boolean dataPacketRequiresRenderUpdate() {
        return true;
    }

    @Override
    protected NBTTagCompound writeToFixedNBT(NBTTagCompound tag) {
        tag = super.writeToFixedNBT(tag);
        if (sealed) {
            tag.setBoolean("Tape", true);
        }
        return tag;
    }

    @Override
    protected void readFromFixedNBT(NBTTagCompound tag) {
        super.readFromFixedNBT(tag);
        sealed = tag.hasKey("Tape") && tag.getBoolean("Tape");
    }

    @Override
    public NBTTagCompound writeToPortableNBT(NBTTagCompound tag) {
        tag = super.writeToPortableNBT(tag);
        tag.setTag("Attrs", attributes.serializeNBT());
        if (owner != null) {
            tag.setString("Own", owner.toString());
        }
        if (securityKey != null) {
            tag.setString("Sec", securityKey);
        }
        return tag;
    }

    @Override
    public void readFromPortableNBT(NBTTagCompound tag) {
        super.readFromPortableNBT(tag);
        if (tag.hasKey("Attrs", Constants.NBT.TAG_COMPOUND)) {
            attributes.deserializeNBT(tag.getCompoundTag("Attrs"));
        } else {
            attributes.setItemLocked(LockAttribute.LOCK_EMPTY, false);
            attributes.setItemLocked(LockAttribute.LOCK_POPULATED, false);
            attributes.setIsConcealed(false);
            attributes.setIsShowingQuantity(false);
        }
        owner = tag.hasKey("Own", Constants.NBT.TAG_STRING) ? UUID.fromString(tag.getString("Own")) : null;
        securityKey = tag.hasKey("Sec", Constants.NBT.TAG_STRING) ? tag.getString("Sec") : null;
    }

    private class TankUpgradeData extends UpgradeData {

        public TankUpgradeData() {
            super(7);
        }

        @Override
        public boolean canAddUpgrade(@Nonnull ItemStack upgrade) {
            Item upgradeItem = upgrade.getItem();
            if (upgradeItem == ModItems.upgradeConversion || upgradeItem == ModItems.upgradeStatus
                    || !super.canAddUpgrade(upgrade)) {
                return false;
            }
            return upgradeItem != ModItems.upgradeOneStack
                    || isCapacityAcceptable(getStorageMultiplier() * getDowngradedBaseCapacity());
        }

        @Override
        public boolean canRemoveUpgrade(int slot) {
            if (!super.canRemoveUpgrade(slot)) {
                return false;
            }
            ItemStack upgrade = getUpgrade(slot);
            if (upgrade.getItem() == ModItems.upgradeStorage) {
                int storageMult = StorageDrawers.config.getStorageUpgradeMultiplier(
                        EnumUpgradeStorage.byMetadata(upgrade.getMetadata()).getLevel());
                return isCapacityAcceptable(Math.max(getStorageMultiplier() - storageMult, 1) * getModifiedBaseCapacity());
            }
            return true;
        }

        private boolean isCapacityAcceptable(int newCapacity) {
            FluidStack fluid = drawerData.getFluidDrawer().getStoredFluid();
            return fluid == null || fluid.amount <= newCapacity;
        }

        @Override
        protected void onUpgradeChanged(ItemStack oldUpgrade, ItemStack newUpgrade) {
            notifyBlockUpdate();
            if (oldUpgrade.getItem() == ModItems.upgradeRedstone) {
                notifyNeighbors();
            }
        }

    }

    private class TankAttributes extends SimpleDrawerAttributes {

        @Override
        protected void onAttributeChanged() {
            notifyBlockUpdate();
        }

    }

}
