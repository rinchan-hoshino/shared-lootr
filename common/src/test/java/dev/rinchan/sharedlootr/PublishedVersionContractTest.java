package dev.rinchan.sharedlootr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        assertEquals("1.1.0", version);
        assertFalse(version.contains("private"));
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
