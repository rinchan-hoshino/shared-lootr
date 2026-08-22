package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VanillaLootMarkerContractTest {

    @Test
    void inventoryAndOpenedStateAreOwnedByTheVanillaChest() throws Exception {
        String randomizable = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/RandomizableContainerBlockEntityMixin.java");
        String chest = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/ChestBlockEntityMixin.java");
        String renderer = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/client/ChestRendererMixin.java");

        assertTrue(randomizable.contains("lootTable != null"));
        assertTrue(randomizable.contains("ChestBlockEntity"));
        assertTrue(chest.contains("wmf_loot_chest_opened"));
        assertTrue(chest.contains("sendBlockUpdated"));
        assertTrue(renderer.contains("marker.wmf$wasOpened()"));
        assertTrue(renderer.contains("LootrChestBlockRenderer.MATERIAL"));
        assertTrue(renderer.contains("LootrChestBlockRenderer.MATERIAL2"));
        assertTrue(renderer.contains("LootrChestBlockRenderer.MATERIAL3"));
        assertTrue(renderer.contains("LootrChestBlockRenderer.MATERIAL4"));
        assertTrue(renderer.contains("LootrAPI.isOldTextures()"));
        assertFalse(renderer.contains("opened_loot_chest.png"));
        assertFalse(renderer.contains("VertexConsumer"));
        assertFalse(Files.exists(resolveSource("common/src/main/resources/assets/shared_lootr/textures/gui/opened_loot_chest.png")));
        assertFalse(randomizable.contains("LootrSavedData"));
        assertFalse(chest.contains("UUID"));
        assertFalse(chest.contains("ServerPlayer"));
    }

    @Test
    void noMixinTargetsLootrInventoryOrPerPlayerOpeners() throws Exception {
        String mixinConfig = readSource("common/src/main/resources/shared_lootr.mixins.json");
        assertFalse(mixinConfig.contains("LootrSavedDataMixin"));
        assertFalse(mixinConfig.contains("ClientOpenersMixin"));
    }

    private static String readSource(String relative) throws Exception {
        return Files.readString(resolveSource(relative));
    }

    private static Path resolveSource(String relative) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(relative);
            if (Files.exists(candidate) || Files.exists(candidate.getParent())) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to find " + relative);
    }
}
