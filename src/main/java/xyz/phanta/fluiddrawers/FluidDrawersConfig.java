package xyz.phanta.fluiddrawers;

import net.minecraftforge.common.config.Config;

@Config(modid = FluidDrawersMod.MOD_ID)
public class FluidDrawersConfig {

    @Config.Comment("The base capacity, in millibuckets (mB), of a basic storage tank (without a capacity downgrade).")
    @Config.RangeInt(min = 1)
    public static int baseCapacity = 32000;

    @Config.Comment("The base capacity, in millibuckets (mB), of a basic storage tank with a capacity downgrade installed.")
    @Config.RangeInt(min = 1)
    public static int baseCapacityDowngraded = 1000;

    @Config.Comment("Whether fluid names should be shown or not when drawers have the quantity display enabled via a quantify key.")
    public static boolean quantifyShowsFluidName = true;

}
