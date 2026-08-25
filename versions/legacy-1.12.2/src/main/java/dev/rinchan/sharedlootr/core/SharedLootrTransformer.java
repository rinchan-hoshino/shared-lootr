package dev.rinchan.sharedlootr.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SharedLootrTransformer implements IClassTransformer {
    private static final String OWNER = "dev/rinchan/rinlib/state/SharedOwnerState";
    private static final Set<String> TARGETS = new HashSet<String>(Arrays.asList(
            "noobanidus.mods.lootr.data.ChestData",
            "noobanidus.mods.lootr.block.tile.LootrChestTileEntity",
            "noobanidus.mods.lootr.client.block.SpecialLootChestTileRenderer",
            "noobanidus.mods.lootr.entity.LootrChestMinecartEntity"
    ));

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGETS.contains(transformedName)) {
            return basicClass;
        }
        ClassReader reader = new ClassReader(basicClass);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM5, writer) {
            @Override
            public MethodVisitor visitMethod(int access, final String methodName, String descriptor, String signature, String[] exceptions) {
                MethodVisitor parent = super.visitMethod(access, methodName, descriptor, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM5, parent) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String calledName, String calledDescriptor, boolean isInterface) {
                        if (shouldReplaceKey(transformedName, methodName, owner, calledName, calledDescriptor)) {
                            if (calledName.equals("put")) {
                                super.visitInsn(Opcodes.SWAP);
                                super.visitInsn(Opcodes.POP);
                                pushSharedOwner();
                                super.visitInsn(Opcodes.SWAP);
                            } else {
                                super.visitInsn(Opcodes.POP);
                                pushSharedOwner();
                            }
                        }
                        super.visitMethodInsn(opcode, owner, calledName, calledDescriptor, isInterface);
                    }

                    private void pushSharedOwner() {
                        super.visitFieldInsn(Opcodes.GETSTATIC, OWNER, "OWNER", "Ljava/util/UUID;");
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static boolean shouldReplaceKey(String className, String methodName, String owner, String calledName, String descriptor) {
        if (className.equals("noobanidus.mods.lootr.data.ChestData")) {
            if (methodName.equals("getInventory") && isMapCall(owner, calledName, descriptor, "get")) return true;
            if (methodName.equals("createInventory") && isMapCall(owner, calledName, descriptor, "put")) return true;
            return methodName.equals("clearInventory") && isMapCall(owner, calledName, descriptor, "remove");
        }
        if (className.equals("noobanidus.mods.lootr.block.tile.LootrChestTileEntity")) {
            return (methodName.equals("stopOpen") || methodName.equals("func_174886_c"))
                    && owner.equals("java/util/Set") && calledName.equals("add");
        }
        if (className.equals("noobanidus.mods.lootr.client.block.SpecialLootChestTileRenderer")) {
            return owner.equals("java/util/Set") && calledName.equals("contains");
        }
        return className.equals("noobanidus.mods.lootr.entity.LootrChestMinecartEntity")
                && methodName.equals("addOpener") && owner.equals("java/util/Set") && calledName.equals("add");
    }

    private static boolean isMapCall(String owner, String calledName, String descriptor, String expectedName) {
        return owner.equals("java/util/Map") && calledName.equals(expectedName)
                && (descriptor.equals("(Ljava/lang/Object;)Ljava/lang/Object;")
                || descriptor.equals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));
    }
}
