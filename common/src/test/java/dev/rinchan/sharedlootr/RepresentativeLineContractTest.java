package dev.rinchan.sharedlootr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static String read(Path path) throws IOException {
        assertTrue(Files.isRegularFile(path), () -> "missing representative source: " + path);
        return Files.readString(path);
    }
}
