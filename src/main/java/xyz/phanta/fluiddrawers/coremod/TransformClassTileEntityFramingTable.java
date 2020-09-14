package xyz.phanta.fluiddrawers.coremod;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;

class TransformClassTileEntityFramingTable extends ClassVisitor {

    TransformClassTileEntityFramingTable(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("isItemValidDrawer") && desc.equals("(Lnet/minecraft/item/ItemStack;)Z")) {
            return new TransformMethodIsItemValidDrawer(api, super.visitMethod(access, name, desc, signature, exceptions));
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private static class TransformMethodIsItemValidDrawer extends MethodVisitor {

        private int state = 0;
        private int shortCircuitJumpInsn = -1;
        @Nullable
        private Label shortCircuitJumpTarget = null;

        TransformMethodIsItemValidDrawer(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            switch (state) {
                case 0:
                    if (opcode == Opcodes.INSTANCEOF
                            && type.equals("com/jaquadro/minecraft/storagedrawers/block/BlockDrawersCustom")) {
                        state = 1;
                    }
                    break;
                case 2:
                    if (opcode == Opcodes.INSTANCEOF
                            && type.equals("com/jaquadro/minecraft/storagedrawers/block/BlockTrimCustom")) {
                        state = 3;
                    }
                    break;
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            switch (state) {
                case 1:
                    state = 2;
                    shortCircuitJumpInsn = opcode;
                    shortCircuitJumpTarget = label;
                    break;
                case 3:
                    state = 4;
                    super.visitJumpInsn(shortCircuitJumpInsn, shortCircuitJumpTarget);
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "xyz/phanta/fluiddrawers/handler/FluidDrawersCoreHooks", "isFramedItem",
                            "(Lnet/minecraft/item/ItemStack;)Z", false);
                    break;
            }
            super.visitJumpInsn(opcode, label);
        }

    }

}
