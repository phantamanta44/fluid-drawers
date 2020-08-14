package xyz.phanta.fluiddrawers.util;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.EnumSet;

// for some reason, BasicDrawerAttributes doesn't deal with the lock attribute enum set being null properly...
public class SimpleDrawerAttributes implements IDrawerAttributes, IDrawerAttributesModifiable, INBTSerializable<NBTTagCompound> {

    @Nullable
    private EnumSet<LockAttribute> lockAttrs = null;
    private boolean concealed = false;
    private boolean showingQty = false;
    private boolean voiding = false;
    private boolean unlimitedStorage = false;
    private boolean vending = false;
    private boolean converting = false;

    @Override
    public boolean canItemLock(LockAttribute attr) {
        return true;
    }

    @Override
    public boolean isItemLocked(LockAttribute attr) {
        return lockAttrs != null && lockAttrs.contains(attr);
    }

    @Override
    public boolean setItemLocked(LockAttribute attr, boolean isLocked) {
        if (isLocked) {
            if (lockAttrs == null) {
                lockAttrs = EnumSet.of(attr);
                onAttributeChanged();
            } else if (lockAttrs.add(attr)) {
                onAttributeChanged();
            }
        } else if (lockAttrs != null && lockAttrs.remove(attr)) {
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public boolean isConcealed() {
        return concealed;
    }

    @Override
    public boolean setIsConcealed(boolean state) {
        if (concealed != state) {
            concealed = state;
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public boolean isVoid() {
        return voiding;
    }

    @Override
    public boolean setIsVoid(boolean state) {
        if (voiding != state) {
            voiding = state;
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public boolean isShowingQuantity() {
        return showingQty;
    }

    @Override
    public boolean setIsShowingQuantity(boolean state) {
        if (showingQty != state) {
            showingQty = state;
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public boolean isUnlimitedStorage() {
        return unlimitedStorage;
    }

    @Override
    public boolean setIsUnlimitedStorage(boolean state) {
        if (unlimitedStorage != state) {
            unlimitedStorage = state;
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public boolean isUnlimitedVending() {
        return vending;
    }

    @Override
    public boolean setIsUnlimitedVending(boolean state) {
        if (vending != state) {
            vending = state;
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public boolean isDictConvertible() {
        return converting;
    }

    @Override
    public boolean setIsDictConvertible(boolean state) {
        if (converting != state) {
            converting = state;
            onAttributeChanged();
        }
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("itemLock", LockAttribute.getBitfield(lockAttrs));
        tag.setBoolean("concealed", concealed);
        tag.setBoolean("void", voiding);
        tag.setBoolean("quant", showingQty);
        tag.setBoolean("unlimited", unlimitedStorage);
        tag.setBoolean("vending", vending);
        tag.setBoolean("conv", converting);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        lockAttrs = LockAttribute.getEnumSet(tag.getInteger("itemLock"));
        concealed = tag.getBoolean("concealed");
        voiding = tag.getBoolean("void");
        showingQty = tag.getBoolean("quant");
        unlimitedStorage = tag.getBoolean("unlimited");
        vending = tag.getBoolean("vending");
        converting = tag.getBoolean("conv");
    }

    protected void onAttributeChanged() {
        // NO-OP
    }

}
