package dev.rinchan.sharedlootr.config;

import java.util.Locale;

/**
 * Controls how Lootr container inventories and opened states are shared across players.
 */
public enum SharingMode {
    /**
     * Every player shares a single server-wide loot pool per Lootr container.
     */
    GLOBAL("GLOBAL"),

    /**
     * Teammates share one pool per container (FTB Teams if installed, otherwise vanilla scoreboard teams).
     */
    TEAMS("TEAMS");

    private final String configValue;

    SharingMode(String configValue) {
        this.configValue = configValue;
    }

    public String configValue() {
        return this.configValue;
    }

    public SharingMode next() {
        return this == GLOBAL ? TEAMS : GLOBAL;
    }

    public static SharingMode parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return GLOBAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("TEAMS".equals(normalized) || "TEAM".equals(normalized)) {
            return TEAMS;
        }
        return GLOBAL;
    }
}
