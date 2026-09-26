package xyz.phanta.fluiddrawers.constant;

import xyz.phanta.fluiddrawers.FdConst;

public class NameConst {

    public static final String BLOCK_TANK = "tank";
    public static final String BLOCK_TANK_CUSTOM = "tank_custom";

    private static final String CONT_KEY = FdConst.MOD_ID + ".container.";
    public static final String CONT_TANK = CONT_KEY + "tank";

    private static final String INFO_KEY = FdConst.MOD_ID + ".info.";
    public static final String INFO_TANK_CAPACITY = INFO_KEY + "tank_capacity";
    public static final String INFO_TANK_CONTENTS = INFO_KEY + "tank_contents";
    public static final String INFO_INFINITE = INFO_KEY + "infinite";
}
