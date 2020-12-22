package xyz.phanta.fluiddrawers.client;

import io.github.phantamanta44.libnine.util.nullity.Reflected;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xyz.phanta.fluiddrawers.CommonProxy;
import xyz.phanta.fluiddrawers.client.handler.ConfigGuiHandler;
import xyz.phanta.fluiddrawers.client.model.FramedTextureModel;

@Reflected
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        ModelLoaderRegistry.registerLoader(new FramedTextureModel.Loader()); // needs to be registered asap
    }

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        MinecraftForge.EVENT_BUS.register(new ConfigGuiHandler());
    }

}
