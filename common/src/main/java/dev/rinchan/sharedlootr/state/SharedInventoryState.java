package dev.rinchan.sharedlootr.state;

import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.ILootrSavedData;

public interface SharedInventoryState {
    boolean sharedLootr$hasSharedInventory();

    static boolean hasSharedInventory(ILootrInfoProvider provider) {
        ILootrSavedData data = LootrAPI.getData(provider);
        return data instanceof SharedInventoryState state && state.sharedLootr$hasSharedInventory();
    }
}
