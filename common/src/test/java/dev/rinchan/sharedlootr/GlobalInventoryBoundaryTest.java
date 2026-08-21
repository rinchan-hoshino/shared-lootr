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
        assertTrue(source.contains("GLOBAL_INVENTORY_OWNER"));
        assertTrue(source.contains("method = {"));
        assertTrue(source.contains("ServerPlayer;Lnoobanidus/mods/lootr/common/api/data/LootFiller;"));
        assertTrue(source.contains("Ljava/util/UUID;Lnoobanidus/mods/lootr/common/api/data/LootFiller;"));
        assertTrue(source.contains("method = \"getInventory"));
        assertTrue(source.contains("@ModifyArg"));
        assertTrue(source.contains("return GLOBAL_INVENTORY_OWNER;"));
        assertFalse(source.contains("player.getUUID()"));
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
