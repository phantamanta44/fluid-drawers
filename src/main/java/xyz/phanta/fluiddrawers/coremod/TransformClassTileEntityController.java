package xyz.phanta.fluiddrawers.coremod;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class TransformClassTileEntityController extends ClassVisitor {

    TransformClassTileEntityController(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("updateCache") && desc.equals("()V")) {
            return new TransformMethodUpdateCache(api, super.visitMethod(access, name, desc, signature, exceptions));
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private static class TransformMethodUpdateCache extends MethodVisitor {

        TransformMethodUpdateCache(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "xyz/phanta/fluiddrawers/handler/FluidDrawersCoreHooks", "updateFluidControllerCache",
                        "(Lcom/jaquadro/minecraft/storagedrawers/block/tile/TileEntityController;)V", false);
            }
            super.visitInsn(opcode);
        }

    }

}
