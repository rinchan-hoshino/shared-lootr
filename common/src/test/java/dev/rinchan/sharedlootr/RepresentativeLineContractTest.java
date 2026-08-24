package dev.rinchan.sharedlootr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepresentativeLineContractTest {
    private static final Path ROOT = Path.of("..").toAbsolutePath().normalize();

    @Test
    void chestDataRepresentativeUsesRinLibAndOneSharedStateAcrossBothLoaders() throws IOException {
        Path line = ROOT.resolve("versions/chest-data-1.20.1");
        String forgeMixin = read(line.resolve(
                "forge/src/main/java/dev/rinchan/sharedlootr/mixin/ForgeChestDataMixin.java"
        ));
        String fabricMixin = read(line.resolve(
                "fabric/src/main/java/dev/rinchan/sharedlootr/mixin/FabricChestDataMixin.java"
        ));
        String forgeBuild = read(line.resolve("forge/build.gradle"));
        String fabricBuild = read(line.resolve("fabric/build.gradle"));
        String forgeMetadata = read(line.resolve("forge/src/main/resources/META-INF/mods.toml"));
        String fabricMetadata = read(line.resolve("fabric/src/main/resources/fabric.mod.json"));
        String forgeJade = read(line.resolve(
                "forge/src/main/java/dev/rinchan/sharedlootr/integration/jade/ForgeSharedLootrJadePlugin.java"
        ));
        String fabricJade = read(line.resolve(
                "fabric/src/main/java/dev/rinchan/sharedlootr/integration/jade/FabricSharedLootrJadePlugin.java"
        ));
        String smoke = read(line.resolve(
                "shared/src/main/java/dev/rinchan/sharedlootr/ChestDataSmokeHarness.java"
        ));

        assertTrue(forgeMixin.contains("noobanidus.mods.lootr.data.ChestData"));
        assertFalse(forgeMixin.contains("net.zestyblaze.lootr.data.ChestData"));
        assertTrue(fabricMixin.contains("net.zestyblaze.lootr.data.ChestData"));
        assertFalse(fabricMixin.contains("noobanidus.mods.lootr.data.ChestData"));
        assertTrue(forgeMixin.contains("SharedOwnerState.OWNER"));
        assertTrue(fabricMixin.contains("SharedOwnerState.OWNER"));
        assertTrue(forgeBuild.contains("dev.rinchan:rinlib-forge"));
        assertTrue(fabricBuild.contains("dev.rinchan:rinlib-fabric"));
        assertTrue(forgeMetadata.contains("modId=\"rinlib\""));
        assertTrue(fabricMetadata.contains("\"rinlib\": \">=0.2.0\""));
        assertTrue(forgeJade.contains("getInventory(SharedOwnerState.OWNER)"));
        assertTrue(fabricJade.contains("getInventory(SharedOwnerState.OWNER)"));
        assertTrue(forgeJade.contains("data.remove(\"Loot\")"));
        assertTrue(fabricJade.contains("data.remove(\"Loot\")"));
        assertTrue(smoke.contains("Legacy player inventory was still readable"));
        assertTrue(smoke.contains("Opened projection was not stored under the shared owner"));
    }

    @Test
    void inventoryStoreLineUsesOneCommonLootrAdapterAndNormalRinLibDependencies() throws IOException {
        Path line = ROOT.resolve("versions/inventory-store-26.1.2");
        String mixin = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/mixin/InventoryStoreMixin.java"));
        String neoBuild = read(line.resolve("neoforge/build.gradle"));
        String fabricBuild = read(line.resolve("fabric/build.gradle"));
        String jade = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/integration/jade/SharedLootrJadePlugin.java"));
        String truth = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/state/SharedInventoryTruth.java"));

        assertTrue(mixin.contains("LootrInventoryStore.class"));
        assertTrue(mixin.contains("SharedOwnerState.get"));
        assertTrue(mixin.contains("SharedOwnerState.put"));
        assertTrue(neoBuild.contains("dev.rinchan:rinlib-neoforge"));
        assertTrue(fabricBuild.contains("dev.rinchan:rinlib-fabric"));
        assertTrue(truth.contains("getInventory(SharedOwnerState.OWNER)"));
        assertTrue(jade.contains("SharedInventoryTruth.exists(instance)"));
        assertTrue(jade.contains("data.remove(\"Loot\")"));

        Path earlyLine = ROOT.resolve("versions/inventory-store-26.1");
        String earlyMixin = read(earlyLine.resolve("shared/src/main/java/dev/rinchan/sharedlootr/mixin/InventoryStoreMixin.java"));
        String earlyHarness = read(earlyLine.resolve("shared/src/main/java/dev/rinchan/sharedlootr/InventoryStoreSmokeHarness.java"));
        String profiles = read(ROOT.resolve("support/inventory-store-profiles.json"));
        assertTrue(earlyMixin.contains("method = \"createInventory\""));
        assertTrue(earlyHarness.contains("store.createInventory(instance, player, filler)"));
        for (String version : List.of("26.1", "26.1.1", "26.1.2", "26.2")) {
            assertTrue(profiles.contains("\"gameVersion\": \"" + version + "\""));
            assertTrue(profiles.contains("\"rinlib_version\": \"0.2.0+" + version + "\""));
        }
    }

    @Test
    void savedDataLineUsesSharedTruthForInventoryAppearanceAndJade() throws IOException {
        Path line = ROOT.resolve("versions/saved-data-1.21.1");
        String mixin = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/mixin/SavedDataInventoryMixin.java"));
        String truth = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/state/SharedSavedDataTruth.java"));
        String jade = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/integration/jade/SharedLootrJadePlugin.java"));
        String harness = read(line.resolve("shared/src/main/java/dev/rinchan/sharedlootr/SavedDataSmokeHarness.java"));
        String fabricBuild = read(line.resolve("fabric/build.gradle"));
        String neoBuild = read(line.resolve("neoforge/build.gradle"));

        assertTrue(mixin.contains("LootrSavedData.class"));
        assertTrue(mixin.contains("SharedOwnerState.get"));
        assertTrue(mixin.contains("SharedOwnerState.put"));
        assertTrue(truth.contains("getInventory(SharedOwnerState.OWNER)"));
        assertTrue(jade.contains("SharedSavedDataTruth.exists(provider)"));
        assertTrue(jade.contains("data.remove(\"Loot\")"));
        assertTrue(harness.contains("Legacy player inventory was still readable"));
        assertTrue(fabricBuild.contains("dev.rinchan:rinlib-fabric"));
        assertTrue(neoBuild.contains("dev.rinchan:rinlib-neoforge"));

        String profiles = read(ROOT.resolve("support/saved-data-profiles.json"));
        for (String version : List.of(
                "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5",
                "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11"
        )) {
            assertTrue(profiles.contains("\"" + version + "\""));
            assertTrue(profiles.contains("\"rinlib_version\": \"0.2.0+" + version + "\""));
        }
        for (String sourceLine : List.of(
                "saved-data-1.21.1", "saved-data-player-legacy-menu",
                "saved-data-player-legacy", "saved-data-player-modern", "saved-data-player"
        )) {
            assertTrue(Files.isDirectory(ROOT.resolve("versions").resolve(sourceLine)));
        }
    }

    private static String read(Path path) throws IOException {
        assertTrue(Files.isRegularFile(path), () -> "missing representative source: " + path);
        return Files.readString(path);
    }
}
