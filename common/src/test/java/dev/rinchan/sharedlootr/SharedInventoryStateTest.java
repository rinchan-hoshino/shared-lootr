package dev.rinchan.sharedlootr;

import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.mixin.SharedInventoryMixin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedInventoryStateTest {
    @Test
    void onlyTheGlobalInventoryKeyRepresentsGeneratedSharedLoot() throws Exception {
        SharedInventoryMixin state = new SharedInventoryMixin() {};
        Map<UUID, Object> inventories = new HashMap<>();
        inventories.put(UUID.fromString("00000000-0000-0000-0000-000000000002"), null);
        setInventories(state, inventories);

        assertFalse(state.sharedLootr$hasSharedInventory());

        inventories.put(SharedOwnerState.OWNER, null);
        assertTrue(state.sharedLootr$hasSharedInventory());
    }

    @Test
    void readAndWriteAdaptersCollapseEveryPlayerIdToOneSharedOwner() throws Exception {
        Method read = SharedInventoryMixin.class.getDeclaredMethod("sharedLootr$readSharedInventory", Object.class);
        Method write = SharedInventoryMixin.class.getDeclaredMethod("sharedLootr$writeSharedInventory", Object.class);
        read.setAccessible(true);
        write.setAccessible(true);
        SharedInventoryMixin state = new SharedInventoryMixin() {};
        var resolvedOwners = new HashSet<>();

        for (int player = 1; player <= 256; player++) {
            UUID playerId = new UUID(0x5348415245440000L, player);
            resolvedOwners.add(read.invoke(state, playerId));
            resolvedOwners.add(write.invoke(state, playerId));
        }

        assertEquals(1, resolvedOwners.size());
        assertEquals(SharedOwnerState.OWNER, resolvedOwners.iterator().next());
    }

    private static void setInventories(SharedInventoryMixin state, Map<UUID, Object> inventories)
            throws ReflectiveOperationException {
        Field field = SharedInventoryMixin.class.getDeclaredField("inventories");
        field.setAccessible(true);
        field.set(state, inventories);
    }
}
