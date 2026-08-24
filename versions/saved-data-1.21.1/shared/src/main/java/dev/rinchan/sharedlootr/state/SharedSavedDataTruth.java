package dev.rinchan.sharedlootr.state;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.data.DataStorage;
import noobanidus.mods.lootr.common.data.LootrSavedData;

public final class SharedSavedDataTruth {
    private SharedSavedDataTruth() {
    }

    public static boolean exists(ILootrInfoProvider provider) {
        return exists(DataStorage.getData(provider));
    }

    public static boolean exists(LootrSavedData data) {
        return data != null && data.getInventory(SharedOwnerState.OWNER) != null;
    }
}
