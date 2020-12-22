package xyz.phanta.fluiddrawers;

import io.github.phantamanta44.libnine.Virtue;
import io.github.phantamanta44.libnine.util.L9CreativeTab;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import xyz.phanta.fluiddrawers.init.FdBlocks;

@Mod(modid = FluidDrawersMod.MOD_ID, version = FluidDrawersMod.VERSION, useMetadata = true)
public class FluidDrawersMod extends Virtue {

    public static final String MOD_ID = "fluiddrawers";
    public static final String VERSION = "1.0.5";

    @Mod.Instance(MOD_ID)
    public static FluidDrawersMod INSTANCE;

    @SidedProxy(
            clientSide = "xyz.phanta.fluiddrawers.client.ClientProxy",
            serverSide = "xyz.phanta.fluiddrawers.CommonProxy")
    public static CommonProxy PROXY;

    @SuppressWarnings("NotNullFieldNotInitialized")
    public static Logger LOGGER;

    public FluidDrawersMod() {
        super(MOD_ID, new L9CreativeTab(MOD_ID, () -> new ItemStack(FdBlocks.TANK)));
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        PROXY.onPreInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        PROXY.onInit(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit(event);
    }

}
