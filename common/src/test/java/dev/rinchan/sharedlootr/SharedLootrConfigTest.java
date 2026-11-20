package dev.rinchan.sharedlootr;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import dev.rinchan.sharedlootr.config.SimpleToml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedLootrConfigTest {
    @Test
    void simpleTomlParsesCommentsQuotesAndFallbacks() {
        String raw = """
                # Top comment
                [section]
                sharingMode = "TEAMS" # inline comment
                soloPlayersSharePool = true
                quotedHash = "alpha # beta"
                invalidBool = maybe
                """;
        Map<String, String> parsed = SimpleToml.parse(raw);
        assertEquals("TEAMS", SimpleToml.getString(parsed, "sharingMode", "GLOBAL"));
        assertTrue(SimpleToml.getBoolean(parsed, "soloPlayersSharePool", false));
        assertEquals("alpha # beta", SimpleToml.getString(parsed, "quotedHash", ""));
        assertFalse(SimpleToml.getBoolean(parsed, "invalidBool", false));
        assertTrue(SimpleToml.getBoolean(parsed, "missingKey", true));
    }

    @Test
    void sharingModeParsingIsCaseInsensitiveWithSafeDefault() {
        assertEquals(SharingMode.GLOBAL, SharingMode.parse(null));
        assertEquals(SharingMode.GLOBAL, SharingMode.parse(""));
        assertEquals(SharingMode.GLOBAL, SharingMode.parse("unknown"));
        assertEquals(SharingMode.GLOBAL, SharingMode.parse("global"));
        assertEquals(SharingMode.TEAMS, SharingMode.parse("teams"));
        assertEquals(SharingMode.TEAMS, SharingMode.parse("TEAM"));
        assertEquals(SharingMode.TEAMS, SharingMode.GLOBAL.next());
        assertEquals(SharingMode.GLOBAL, SharingMode.TEAMS.next());
    }

    @Test
    void configInitializesSavesAndHotReloadsFromDisk(@TempDir Path tempDir) throws Exception {
        SharedLootrConfig initial = SharedLootrConfig.init(tempDir);
        assertEquals(SharingMode.GLOBAL, initial.sharingMode());
        assertFalse(initial.soloPlayersSharePool());

        Path tomlFile = tempDir.resolve(SharedLootrConfig.FILE_NAME);
        assertTrue(Files.exists(tomlFile));

        SharedLootrConfig updated = new SharedLootrConfig(SharingMode.TEAMS, true);
        SharedLootrConfig.updateAndSave(updated);
        assertEquals(updated, SharedLootrConfig.get());

        SharedLootrConfig reloaded = SharedLootrConfig.reload();
        assertEquals(SharingMode.TEAMS, reloaded.sharingMode());
        assertTrue(reloaded.soloPlayersSharePool());

        // Simulate external hand-edit and hot reload
        Files.writeString(tomlFile, "sharingMode = \"GLOBAL\"\nsoloPlayersSharePool = false\n", StandardCharsets.UTF_8);
        SharedLootrConfig hotReloaded = SharedLootrConfig.reload();
        assertEquals(SharingMode.GLOBAL, hotReloaded.sharingMode());
        assertFalse(hotReloaded.soloPlayersSharePool());
    }
}
