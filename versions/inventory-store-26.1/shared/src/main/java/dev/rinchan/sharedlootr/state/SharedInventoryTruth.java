package dev.rinchan.sharedlootr.state;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.data.DataStorage;
import noobanidus.mods.lootr.common.data.LootrInventoryStore;

public final class SharedInventoryTruth {
    private SharedInventoryTruth() {
    }

    public static boolean exists(ILootrContainerInstance instance) {
        return exists(DataStorage.getData(instance));
    }

    public static boolean exists(LootrInventoryStore store) {
        return store != null && store.getInventory(SharedOwnerState.OWNER) != null;
    }
}
