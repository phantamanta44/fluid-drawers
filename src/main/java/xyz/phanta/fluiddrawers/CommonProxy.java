package xyz.phanta.fluiddrawers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import xyz.phanta.fluiddrawers.handler.ControllerFluidCapabilityHandler;
import xyz.phanta.fluiddrawers.integration.FramedDrawerHandler;
import xyz.phanta.fluiddrawers.network.SPacketSyncFluidDrawerCount;
import xyz.phanta.fluiddrawers.network.SPacketSyncFluidDrawerFluid;

public class CommonProxy {

    public void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ControllerFluidCapabilityHandler());
        if (Loader.isModLoaded("framedcompactdrawers")) {
            MinecraftForge.EVENT_BUS.register(new FramedDrawerHandler());
        }
        SimpleNetworkWrapper netHandler = FluidDrawersMod.INSTANCE.getNetworkHandler();
        netHandler.registerMessage(SPacketSyncFluidDrawerFluid.Handler.class, SPacketSyncFluidDrawerFluid.class, 0, Side.CLIENT);
        netHandler.registerMessage(SPacketSyncFluidDrawerCount.Handler.class, SPacketSyncFluidDrawerCount.class, 1, Side.CLIENT);
    }

    public void onInit(FMLInitializationEvent event) {
        // NO-OP
    }

    public void onPostInit(FMLPostInitializationEvent event) {
        // NO-OP
    }

}
