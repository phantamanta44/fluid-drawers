package xyz.phanta.fluiddrawers.init;

import io.github.phantamanta44.libnine.InitMe;
import io.github.phantamanta44.libnine.LibNine;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.block.BlockTank;
import xyz.phanta.fluiddrawers.block.BlockTankCustom;
import xyz.phanta.fluiddrawers.client.tesr.RenderTileTank;
import xyz.phanta.fluiddrawers.constant.NameConst;
import xyz.phanta.fluiddrawers.tile.TileTank;
import xyz.phanta.fluiddrawers.tile.TileTankCustom;

public class FdBlocks {

    @GameRegistry.ObjectHolder(FluidDrawersMod.MOD_ID + ":" + NameConst.BLOCK_TANK)
    public static BlockTank TANK;

    @GameRegistry.ObjectHolder(FluidDrawersMod.MOD_ID + ":" + NameConst.BLOCK_TANK_CUSTOM)
    public static BlockTankCustom TANK_CUSTOM;

    @InitMe(FluidDrawersMod.MOD_ID)
    public static void init() {
        new BlockTank();
        new BlockTankCustom();
    }

    @SideOnly(Side.CLIENT)
    @InitMe(sides = { Side.CLIENT })
    public static void initClient() {
        LibNine.PROXY.getRegistrar().queueTESRReg(TileTank.class, new RenderTileTank<>());
        LibNine.PROXY.getRegistrar().queueTESRReg(TileTankCustom.class, new RenderTileTank<>());
    }

}
