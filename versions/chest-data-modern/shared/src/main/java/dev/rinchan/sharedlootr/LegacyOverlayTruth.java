package dev.rinchan.sharedlootr;

import dev.rinchan.rinlib.state.SharedOwnerState;

import java.util.Map;
import java.util.UUID;

/** Stable Waila/Jade-equivalent adapter for releases whose overlay APIs are loader-specific. */
public final class LegacyOverlayTruth {
    private LegacyOverlayTruth() {
    }

    public static boolean hasSharedInventory(Map<UUID, ?> inventories) {
        return SharedOwnerState.contains(inventories);
    }
}
