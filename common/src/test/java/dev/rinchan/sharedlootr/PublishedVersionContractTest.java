package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class PublishedVersionContractTest {
    @Test
    void releaseUsesStandardPublicVersionWithoutPrivateSuffix() throws Exception {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(findProjectFile("gradle.properties"))) {
            properties.load(reader);
        }
        String version = properties.getProperty("mod_version");
        assertEquals("1.2.0", version);
        assertFalse(version.contains("private"));
        assertEquals("GPL-3.0-or-later", properties.getProperty("mod_license"));
    }

    @Test
    void releaseDocumentsTheIntentionalWorldDataBreak() throws Exception {
        String readme = Files.readString(findProjectFile("README.md"));
        assertTrue(readme.contains("## Incompatible upgrade"));
        assertTrue(readme.contains("does not migrate"));
        assertTrue(readme.contains("previously collected loot can become available again"));
        assertTrue(readme.contains("remain stored but are not used"));
    }

    private static Path findProjectFile(String relative) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(relative);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to locate " + relative);
    }
}
