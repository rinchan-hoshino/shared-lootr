package dev.rinchan.sharedlootr.state;

public interface SharedInventoryState {
    boolean sharedLootr$hasSharedInventory();

    static boolean hasSharedInventory(Object data) {
        return data instanceof SharedInventoryState
                && ((SharedInventoryState) data).sharedLootr$hasSharedInventory();
    }
}
