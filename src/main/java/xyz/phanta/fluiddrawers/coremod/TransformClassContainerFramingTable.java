package xyz.phanta.fluiddrawers.coremod;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class TransformClassContainerFramingTable extends ClassVisitor {

    TransformClassContainerFramingTable(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ((name.equals("onCraftMatrixChanged") || name.equals("func_75130_a"))
                && desc.equals("(Lnet/minecraft/inventory/IInventory;)V")) {
            return new TransformMethodOnCraftMatrixChanged(api, super.visitMethod(access, name, desc, signature, exceptions));
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private static class TransformMethodOnCraftMatrixChanged extends MethodVisitor {

        private boolean good = false;

        TransformMethodOnCraftMatrixChanged(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            switch (opcode) {
                case Opcodes.GETFIELD:
                    if (name.equals("craftResult")
                            && owner.equals("com/jaquadro/minecraft/storagedrawers/inventory/ContainerFramingTable")) {
                        good = true;
                    }
                    break;
                case Opcodes.GETSTATIC:
                    if (good && (name.equals("EMPTY") || name.equals("field_190927_a")) && owner.equals("net/minecraft/item/ItemStack")) {
                        super.visitVarInsn(Opcodes.ALOAD, 2);
                        super.visitVarInsn(Opcodes.ALOAD, 3);
                        super.visitVarInsn(Opcodes.ALOAD, 4);
                        super.visitVarInsn(Opcodes.ALOAD, 5);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                "xyz/phanta/fluiddrawers/handler/FluidDrawersCoreHooks", "getFramedItemStack",
                                "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;" +
                                        "Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", false);
                        return;
                    }
                    break;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            super.visitLineNumber(line, start);
            good = false;
        }

    }

}
