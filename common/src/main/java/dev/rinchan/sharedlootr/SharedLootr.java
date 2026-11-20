package dev.rinchan.sharedlootr;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Shared Lootr entrypoint constants and configuration bootstrap.
 */
public final class SharedLootr {
    public static final String MOD_ID = "shared_lootr";
    public static final String MOD_NAME = "Shared Lootr";

    /**
     * Fixed global shared pool UUID kept 100% compatible with Shared Lootr 1.x saves.
     */
    public static final UUID GLOBAL_POOL_UUID = new UUID(0L, 1L);

    private SharedLootr() {
    }

    public static void init(Path configDir) {
        SharedLootrConfig.init(configDir);
    }
}
