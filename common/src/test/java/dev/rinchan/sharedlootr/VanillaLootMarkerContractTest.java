package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VanillaLootMarkerContractTest {

    @Test
    void sharingInterceptsOnlyLootrInventoryOwnership() throws Exception {
        String savedData = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/LootrSavedDataMixin.java");
        String instance = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/SimpleLootrInstanceMixin.java");
        String openBroadcast = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/DefaultLootrAPIImplMixin.java");

        assertTrue(savedData.contains("LootrSavedData"));
        assertTrue(savedData.contains("GLOBAL_INVENTORY_OWNER"));
        assertTrue(savedData.contains("createInventory"));
        assertTrue(savedData.contains("common/api/data/ILootrInfoProvider"));
        assertTrue(savedData.contains("common/api/data/LootFiller"));
        assertFalse(savedData.contains("common/api/ILootrInfoProvider"));
        assertFalse(savedData.contains("common/api/LootFiller"));
        assertTrue(savedData.contains("selectCanonicalInventory"));
        assertTrue(instance.contains("NBTConstants.HAS_BEEN_OPENED"));
        assertTrue(instance.contains("clientOpened = tag.getBoolean"));
        assertTrue(instance.contains("sharedLootr$keepGlobalOpenedVisual"));
        assertTrue(instance.contains("ci.cancel()"));
        assertTrue(openBroadcast.contains(
                "handleProviderOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;"
                        + "Lnet/minecraft/server/level/ServerPlayer;"
                        + "Lnoobanidus/mods/lootr/common/api/MenuBuilder;)V"
        ));
        assertTrue(openBroadcast.contains("addOpener(Lnet/minecraft/world/entity/player/Player;)Z"));
        assertTrue(openBroadcast.contains("boolean added = provider.addOpener(player)"));
        assertTrue(openBroadcast.contains("if (!added)"));
        assertTrue(openBroadcast.contains("provider.performOpen()"));
    }

    @Test
    void lootrRemainsTheOnlyContainerAndRendererOwner() throws Exception {
        String mixins = readSource("common/src/main/resources/shared_lootr.mixins.json");
        assertTrue(mixins.contains("LootrSavedDataMixin"));
        assertTrue(mixins.contains("SimpleLootrInstanceMixin"));
        assertTrue(mixins.contains("DefaultLootrAPIImplMixin"));
        assertFalse(mixins.contains("ChestBlockEntityMixin"));
        assertFalse(mixins.contains("RandomizableContainerBlockEntityMixin"));
        assertFalse(mixins.contains("ChestRendererMixin"));
        assertFalse(Files.exists(resolveSource("common/src/main/java/dev/rinchan/sharedlootr/marker/LootChestMarker.java")));
        assertFalse(Files.exists(resolveSource("common/src/main/resources/assets/shared_lootr/textures")));
    }

    private static String readSource(String relative) throws Exception {
        return Files.readString(resolveSource(relative));
    }

    private static Path resolveSource(String relative) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("common/src/main"))) {
                return current.resolve(relative);
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to find repository root");
    }
}
