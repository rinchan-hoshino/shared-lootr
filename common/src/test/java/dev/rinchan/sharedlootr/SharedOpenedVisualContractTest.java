package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SharedOpenedVisualContractTest {
    @Test
    void openedBlockEntitiesBroadcastTheirPersistentGlobalMarker() throws IOException {
        String source = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/LootrBlockEntityMixin.java");
        assertTrue(source.contains("LootrChestBlockEntity.class"));
        assertTrue(source.contains("LootrBarrelBlockEntity.class"));
        assertTrue(source.contains("LootrShulkerBlockEntity.class"));
        assertTrue(source.contains("LootrDecoratedPotBlockEntity.class"));
        assertTrue(source.contains("LootrBrushableBlockEntity.class"));
        assertTrue(source.contains("method = \"markChanged\""));
        assertTrue(source.contains("provider.hasBeenOpened()"));
        assertTrue(source.contains("sendBlockUpdated"));
        assertTrue(source.contains("Block.UPDATE_CLIENTS"));

        String mixins = readSource("common/src/main/resources/shared_lootr.mixins.json");
        assertTrue(mixins.contains("\"LootrBlockEntityMixin\""));
    }

    @Test
    void openedRenderingUsesOnlyTheGlobalMarkerAndNeverThePlayerUuid() throws IOException {
        String source = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/ClientOpenersMixin.java");
        assertTrue(source.contains("IClientOpeners.class"));
        assertTrue(source.contains("method = \"hasClientOpened\""));
        assertTrue(source.contains("self.isClientOpened()"));
        assertTrue(source.contains("UUID ignoredPlayerId"));

        String mixins = readSource("common/src/main/resources/shared_lootr.mixins.json");
        assertTrue(mixins.contains("\"ClientOpenersMixin\""));
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
