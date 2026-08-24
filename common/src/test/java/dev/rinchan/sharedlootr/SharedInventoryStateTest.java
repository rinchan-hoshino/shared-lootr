package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.mixin.LootrSavedDataMixin;
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

        inventories.put(SharedOwnerState.OWNER, null);
        assertTrue(state.sharedLootr$hasSharedInventory());
    }

    private static void setInventories(LootrSavedDataMixin state, Map<UUID, Object> inventories)
            throws ReflectiveOperationException {
        Field field = LootrSavedDataMixin.class.getDeclaredField("inventories");
        field.setAccessible(true);
        field.set(state, inventories);
    }
}
