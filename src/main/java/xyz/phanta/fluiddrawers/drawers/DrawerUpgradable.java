package xyz.phanta.fluiddrawers.drawers;

import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;

public interface DrawerUpgradable {

    UpgradeData getUpgrades();

    void notifyChanged();

}
