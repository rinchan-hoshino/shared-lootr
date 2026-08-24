package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VanillaLootMarkerContractTest {

    @Test
    void sharingInterceptsOnlyLootrInventoryOwnership() throws Exception {
        String savedData = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/SharedInventoryMixin.java");
        String instance = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/SimpleLootrInstanceMixin.java");
        String openBroadcast = readSource("common/src/main/java/dev/rinchan/sharedlootr/mixin/DefaultLootrAPIImplMixin.java");

        assertTrue(savedData.contains("noobanidus.mods.lootr.common.data.LootrSavedData"));
        assertTrue(savedData.contains("implements SharedInventoryState"));
        assertTrue(savedData.contains("SharedOwnerState.OWNER"));
        assertTrue(savedData.contains("SharedOwnerState.contains(inventories)"));
        assertTrue(savedData.contains("Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"));
        assertTrue(savedData.contains("Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));
        assertTrue(instance.contains("NBTConstants.HAS_BEEN_OPENED"));
        assertTrue(instance.contains("StickyBoolean.next"));
        assertTrue(instance.contains("sharedLootr$keepGlobalOpenedVisual"));
        assertTrue(openBroadcast.contains(
                "handleProviderOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;"
                        + "Lnet/minecraft/server/level/ServerPlayer;"
                        + "Lnoobanidus/mods/lootr/common/api/MenuBuilder;)V"
        ));
        assertTrue(openBroadcast.contains("performOpen(Lnet/minecraft/server/level/ServerPlayer;)V"));
        assertTrue(openBroadcast.contains("SharedInventoryState.hasSharedInventory(provider)"));
        assertTrue(openBroadcast.contains("provider.performOpen()"));
    }

    @Test
    void jadeStopsCallingGeneratedSharedLootUnopened() throws Exception {
        String integration = readSource(
                "common/src/main/java/dev/rinchan/sharedlootr/integration/jade/SharedLootrJadePlugin.java"
        );

        assertTrue(integration.contains("registration.registerBlockDataProvider"));
        assertTrue(integration.contains("BlockEntity.class"));
        assertTrue(integration.contains("instanceof ILootrInfoProvider provider"));
        assertTrue(integration.contains("SharedInventoryState.hasSharedInventory(provider)"));
        assertTrue(integration.contains("tag.remove(\"Loot\")"));
        assertTrue(integration.contains("return 1100"));
        assertFalse(integration.contains("LootrAPI.getData"));
        assertFalse(integration.contains("LootrAPI.getInventory"));
        assertFalse(integration.contains("tooltip.remove"));
    }

    @Test
    void lootrRemainsTheOnlyContainerAndRendererOwner() throws Exception {
        String mixins = readSource("common/src/main/resources/shared_lootr.mixins.json");
        assertTrue(mixins.contains("SharedInventoryMixin"));
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
