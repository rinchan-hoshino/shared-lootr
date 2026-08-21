package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GlobalInventoryBoundaryTest {
    @Test
    void everyLootrInventoryLookupUsesOneServerGlobalOwner() throws IOException {
        String source = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/LootrSavedDataMixin.java");
        assertTrue(source.contains("method = \"getInventory"));
        assertTrue(source.contains("selectCanonicalInventory()"));
        assertTrue(source.contains("inventories.size() > 1"));
        assertTrue(source.contains("inventories.put(owner, inventory)"));
        assertTrue(source.contains("occupiedSlots(candidate.getValue())"));
        assertTrue(source.contains("cir.setReturnValue(inventory)"));
        assertFalse(source.contains("@ModifyArg"));
        assertFalse(source.contains("inventories.remove(player.getUUID())"));
    }

    private static String readSource(String relative) throws IOException {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(relative);
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
            current = current.getParent();
        }
        throw new IOException("Unable to locate " + relative);
    }
}
