package xyz.phanta.fluiddrawers.tile;

import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.MaterialData;
import io.github.phantamanta44.libnine.tile.RegisterTile;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.tile.base.FramedTile;

@RegisterTile(FluidDrawersMod.MOD_ID)
public class TileTankCustom extends TileTank implements FramedTile {

    private final MaterialData materialData = new MaterialData();

    public TileTankCustom() {
        injectData(materialData);
    }

    @Override
    public MaterialData getMaterialData() {
        return materialData;
    }

}
