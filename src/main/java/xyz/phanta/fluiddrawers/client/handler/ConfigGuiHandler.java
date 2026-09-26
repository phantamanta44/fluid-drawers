package xyz.phanta.fluiddrawers.client.handler;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.phanta.fluiddrawers.FdConst;

public class ConfigGuiHandler {

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(FdConst.MOD_ID)) {
            ConfigManager.sync(FdConst.MOD_ID, Config.Type.INSTANCE);
        }
    }

}
