package xyz.phanta.fluiddrawers.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.function.Function;

public class FluidDrawersClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        switch (transformedName) {
            case "com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController":
                return doTransform(code, TransformClassTileEntityController::new, 0, 0);
            case "com.jaquadro.minecraft.storagedrawers.inventory.ContainerFramingTable":
                return doTransform(code, TransformClassContainerFramingTable::new, 0, 0);
            case "com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityFramingTable":
                return doTransform(code, TransformClassTileEntityFramingTable::new, 0, 0);
//            case "com.jaquadro.minecraft.storagedrawers.client.renderer.TileEntityFramingRenderer":
//                return doTransform(code, TransformClassTileEntityFramingRenderer::new, 0, 0);
        }
        return code;
    }

    private static byte[] doTransform(byte[] code, Function<ClassVisitor, ClassVisitor> mapperFactory,
                                      int readFlags, int writeFlags) {
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader, writeFlags);
        reader.accept(mapperFactory.apply(writer), readFlags);
        return writer.toByteArray();
    }

}
