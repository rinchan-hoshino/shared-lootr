package dev.rinchan.sharedlootr;

import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.mixin.SharedInventoryMixin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private static void setInventories(SharedInventoryMixin state, Map<UUID, Object> inventories)
            throws ReflectiveOperationException {
        Field field = SharedInventoryMixin.class.getDeclaredField("inventories");
        field.setAccessible(true);
        field.set(state, inventories);
    }
}
