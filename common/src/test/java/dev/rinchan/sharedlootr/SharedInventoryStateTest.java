package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rinchan.sharedlootr.mixin.LootrSavedDataMixin;
import dev.rinchan.sharedlootr.state.SharedInventoryState;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SharedInventoryStateTest {
    @Test
    void onlyTheSharedInventoryKeyDefinesGeneratedState() throws Exception {
        LootrSavedDataMixin state = new LootrSavedDataMixin() {};
        Map<UUID, Object> inventories = new HashMap<>();
        setInventories(state, inventories);

        inventories.put(UUID.randomUUID(), null);
        assertFalse(state.sharedLootr$hasSharedInventory());

        inventories.put(SharedInventoryState.GLOBAL_INVENTORY_OWNER, null);
        assertTrue(state.sharedLootr$hasSharedInventory());
    }

    private static void setInventories(LootrSavedDataMixin state, Map<UUID, Object> inventories)
            throws ReflectiveOperationException {
        Field field = LootrSavedDataMixin.class.getDeclaredField("inventories");
        field.setAccessible(true);
        field.set(state, inventories);
    }
}
