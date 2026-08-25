package dev.rinchan.sharedlootr.core;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class SharedLootrTransformerTest {
    @Test
    public void transformsInventoryKeysAndClientAppearanceConsumersToSharedOwner() throws Exception {
        assertOwnerProjection("noobanidus.mods.lootr.data.ChestData", "java/util/Map", "get", "put", "remove");
        assertOwnerProjection("noobanidus.mods.lootr.client.block.SpecialLootChestTileRenderer", "java/util/Set", "contains");
        assertOwnerProjection("noobanidus.mods.lootr.block.tile.LootrChestTileEntity", "java/util/Set", "add");
        assertOwnerProjection("noobanidus.mods.lootr.entity.LootrChestMinecartEntity", "java/util/Set", "add");
    }

    private static void assertOwnerProjection(String className, String owner, String... calls) throws Exception {
        String resource = className.replace('.', '/') + ".class";
        InputStream input = SharedLootrTransformerTest.class.getClassLoader().getResourceAsStream(resource);
        assertTrue("Missing Lootr class " + className, input != null);
        byte[] original = readAll(input);
        byte[] transformed = new SharedLootrTransformer().transform(className, className, original);

        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        for (String call : calls) {
            boolean projected = false;
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode invocation = (MethodInsnNode) instruction;
                    if (!invocation.owner.equals(owner) || !invocation.name.equals(call)) continue;
                    AbstractInsnNode previous = previousReal(instruction.getPrevious());
                    if (call.equals("put")) previous = previousReal(previous.getPrevious());
                    if (previous instanceof FieldInsnNode) {
                        FieldInsnNode field = (FieldInsnNode) previous;
                        if (field.owner.equals("dev/rinchan/rinlib/state/SharedOwnerState") && field.name.equals("OWNER")) {
                            projected = true;
                        }
                    }
                }
            }
            assertTrue(className + " did not project " + owner + "." + call + " to SharedOwnerState.OWNER", projected);
        }
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode node) {
        while (node != null && node.getOpcode() < 0) node = node.getPrevious();
        return node;
    }

    private static byte[] readAll(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0; ) output.write(buffer, 0, read);
            return output.toByteArray();
        } finally {
            input.close();
        }
    }
}
