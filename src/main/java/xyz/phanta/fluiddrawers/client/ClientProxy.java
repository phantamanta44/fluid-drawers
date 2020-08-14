package xyz.phanta.fluiddrawers.client;

import io.github.phantamanta44.libnine.util.nullity.Reflected;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import xyz.phanta.fluiddrawers.CommonProxy;
import xyz.phanta.fluiddrawers.client.model.FramedTextureModel;

@Reflected
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        ModelLoaderRegistry.registerLoader(new FramedTextureModel.Loader()); // needs to be registered asap
    }

}
