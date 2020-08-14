/*package xyz.phanta.fluiddrawers.coremod;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class TransformClassTileEntityFramingRenderer extends ClassVisitor {

    TransformClassTileEntityFramingRenderer(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("render")
                && desc.equals("(Lcom/jaquadro/minecraft/storagedrawers/block/tile/TileEntityFramingTable;DDDFIF)V")) {
            return new TransformMethodRender(api, super.visitMethod(access, name, desc, signature, exceptions));
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private static class TransformMethodRender extends MethodVisitor {

        TransformMethodRender(int api, MethodVisitor mv) {
            super(api, mv);
        }

    }

}
*/
// TODO implement
