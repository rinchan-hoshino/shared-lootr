package dev.rinchan.sharedlootr.config;

import dev.rinchan.sharedlootr.SharedLootr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime configuration stored at {@code config/shared_lootr.toml} with zero external dependencies.
 */
public final class SharedLootrConfig {
    public static final String FILE_NAME = SharedLootr.MOD_ID + ".toml";
    private static final Logger LOGGER = LoggerFactory.getLogger(SharedLootr.MOD_NAME);

    private static volatile SharedLootrConfig active = defaults();
    private static volatile Path configPath;

    private final SharingMode sharingMode;
    private final boolean soloPlayersSharePool;

    public SharedLootrConfig(SharingMode sharingMode, boolean soloPlayersSharePool) {
        this.sharingMode = Objects.requireNonNull(sharingMode, "sharingMode");
        this.soloPlayersSharePool = soloPlayersSharePool;
    }

    public static SharedLootrConfig defaults() {
        return new SharedLootrConfig(SharingMode.GLOBAL, false);
    }

    public static SharedLootrConfig get() {
        return active;
    }

    public SharingMode sharingMode() {
        return this.sharingMode;
    }

    public boolean soloPlayersSharePool() {
        return this.soloPlayersSharePool;
    }

    public static synchronized SharedLootrConfig init(Path configDir) {
        Objects.requireNonNull(configDir, "configDir");
        configPath = configDir.resolve(FILE_NAME);
        return reload();
    }

    public static synchronized SharedLootrConfig reload() {
        Path target = configPath;
        if (target == null) {
            active = defaults();
            return active;
        }
        if (Files.notExists(target)) {
            SharedLootrConfig def = defaults();
            active = def;
            writeFile(target, def);
            return def;
        }
        try {
            String raw = Files.readString(target, StandardCharsets.UTF_8);
            SharedLootrConfig parsed = fromToml(raw);
            active = parsed;
            return parsed;
        } catch (IOException ex) {
            LOGGER.warn("Failed to read {}, falling back to defaults: {}", target, ex.getMessage());
            SharedLootrConfig def = defaults();
            active = def;
            return def;
        }
    }

    public static synchronized void updateAndSave(SharedLootrConfig updated) {
        Objects.requireNonNull(updated, "updated");
        active = updated;
        Path target = configPath;
        if (target != null) {
            writeFile(target, updated);
        }
    }

    public static SharedLootrConfig fromToml(String content) {
        Map<String, String> entries = SimpleToml.parse(content);
        SharingMode mode = SharingMode.parse(SimpleToml.getString(entries, "sharingMode", SharingMode.GLOBAL.configValue()));
        boolean soloShare = SimpleToml.getBoolean(entries, "soloPlayersSharePool", false);
        return new SharedLootrConfig(mode, soloShare);
    }

    public String toToml() {
        return "# Shared Lootr configuration\n"
                + "#\n"
                + "# sharingMode:\n"
                + "#   \"GLOBAL\" - Everyone shares one pool per Lootr container (default, 100% compatible with 1.x).\n"
                + "#   \"TEAMS\"  - Teammates share a pool (FTB Teams if installed, otherwise vanilla scoreboard team).\n"
                + "sharingMode = " + SimpleToml.quote(this.sharingMode.configValue()) + "\n"
                + "\n"
                + "# Only used when sharingMode = \"TEAMS\".\n"
                + "# false (default): Players not in any team keep their own private loot pool.\n"
                + "# true: Players not in any team share a common solo pool.\n"
                + "soloPlayersSharePool = " + this.soloPlayersSharePool + "\n";
    }

    private static void writeFile(Path target, SharedLootrConfig cfg) {
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(target, cfg.toToml(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.warn("Failed to save {}: {}", target, ex.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SharedLootrConfig other)) {
            return false;
        }
        return this.sharingMode == other.sharingMode
                && this.soloPlayersSharePool == other.soloPlayersSharePool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sharingMode, this.soloPlayersSharePool);
    }

    @Override
    public String toString() {
        return "SharedLootrConfig{sharingMode=" + this.sharingMode
                + ", soloPlayersSharePool=" + this.soloPlayersSharePool + "}";
    }
}
