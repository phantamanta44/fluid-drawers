package xyz.phanta.fluiddrawers.init;

import io.github.phantamanta44.libnine.InitMe;
import io.github.phantamanta44.libnine.gui.GuiIdentity;
import io.github.phantamanta44.libnine.gui.L9GuiHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.phanta.fluiddrawers.FluidDrawersMod;
import xyz.phanta.fluiddrawers.client.gui.GuiTank;
import xyz.phanta.fluiddrawers.inventory.ContainerTank;

import java.util.Objects;

public class FdGuis {

    public static final GuiIdentity<ContainerTank, GuiTank> TANK = new GuiIdentity<>("tank", ContainerTank.class);

    @InitMe
    public static void init() {
        L9GuiHandler guiHandler = FluidDrawersMod.INSTANCE.getGuiHandler();
        guiHandler.registerServerGui(TANK, (p, w, x, y, z) -> new ContainerTank(p, getTile(w, x, y, z)));
    }

    @SideOnly(Side.CLIENT)
    @InitMe(sides = { Side.CLIENT })
    public static void initClient() {
        L9GuiHandler guiHandler = FluidDrawersMod.INSTANCE.getGuiHandler();
        guiHandler.registerClientGui(TANK, (c, p, w, x, y, z) -> new GuiTank(c));
    }

    @SuppressWarnings("unchecked")
    private static <T extends TileEntity> T getTile(World world, int x, int y, int z) {
        // should we really be asserting non-null here?
        return Objects.requireNonNull((T)world.getTileEntity(new BlockPos(x, y, z)));
    }

}
