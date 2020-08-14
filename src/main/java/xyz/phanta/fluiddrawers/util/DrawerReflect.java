package xyz.phanta.fluiddrawers.util;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import io.github.phantamanta44.libnine.util.helper.MirrorUtils;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class DrawerReflect {

    private static final Class<?> tTileEntityController$StorageRecord;

    static {
        try {
            tTileEntityController$StorageRecord = Class.forName(
                    "com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController$StorageRecord");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't initialize Storage Drawers reflection!", e);
        }
    }

    private static final MirrorUtils.IField<Map<BlockPos, ?>> fTileEntityController_storage
            = MirrorUtils.reflectField(TileEntityController.class, "storage");
    private static final MirrorUtils.IField<IDrawerGroup> fTileEntityController$StorageRecord_storage
            = MirrorUtils.reflectField(tTileEntityController$StorageRecord, "storage");

    public static Map<BlockPos, ?> getStorageRecords(TileEntityController tile) {
        return fTileEntityController_storage.get(tile);
    }

    public static IDrawerGroup getGroupForRecord(Object storageRecord) {
        return fTileEntityController$StorageRecord_storage.get(storageRecord);
    }

}
