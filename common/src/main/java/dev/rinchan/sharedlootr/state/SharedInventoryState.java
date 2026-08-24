package dev.rinchan.sharedlootr.state;

import java.util.UUID;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.ILootrSavedData;

public interface SharedInventoryState {
    UUID GLOBAL_INVENTORY_OWNER = new UUID(0L, 1L);

    boolean sharedLootr$hasSharedInventory();

    static boolean hasSharedInventory(ILootrInfoProvider provider) {
        ILootrSavedData data = LootrAPI.getData(provider);
        return data instanceof SharedInventoryState state && state.sharedLootr$hasSharedInventory();
    }
}
