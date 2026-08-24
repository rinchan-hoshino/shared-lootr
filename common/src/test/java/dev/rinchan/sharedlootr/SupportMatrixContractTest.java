package dev.rinchan.sharedlootr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportMatrixContractTest {
    private static final Path ROOT = Path.of("..").toAbsolutePath().normalize();

    @Test
    void coversEveryAcceptedLootrVersionAndLoaderCell() throws IOException {
        JsonObject matrix = JsonParser.parseString(
            Files.readString(ROOT.resolve("support/lootr-matrix.json"))
        ).getAsJsonObject();
        JsonArray targets = matrix.getAsJsonArray("targets");

        assertEquals(33, matrix.get("labelCount").getAsInt());
        assertEquals(60, matrix.get("cellCount").getAsInt());
        assertEquals(60, targets.size());

        Set<String> labels = new HashSet<>();
        Set<String> cells = new HashSet<>();
        Map<String, Integer> eraCounts = new HashMap<>();
        for (var element : targets) {
            JsonObject target = element.getAsJsonObject();
            String gameVersion = target.get("gameVersion").getAsString();
            String loader = target.get("loader").getAsString();
            labels.add(gameVersion);
            assertTrue(cells.add(gameVersion + ":" + loader));
            eraCounts.merge(target.get("era").getAsString(), 1, Integer::sum);

            JsonObject lootr = target.getAsJsonObject("lootr");
            assertEquals("EltpO5cN", lootr.get("projectId").getAsString());
            assertEquals(128, lootr.get("sha512").getAsString().length());
            assertTrue(target.get("evidence").getAsString().length() > 10);
        }

        assertEquals(33, labels.size());
        assertEquals(Map.of(
            "legacy_1_12", 1,
            "chest_data", 27,
            "saved_data", 24,
            "inventory_store", 8
        ), eraCounts);
        assertTrue(cells.contains("22w24a:fabric"));
        assertTrue(cells.contains("26.1.1:fabric"));
        assertTrue(cells.contains("26.1.1:neoforge"));
    }
}
