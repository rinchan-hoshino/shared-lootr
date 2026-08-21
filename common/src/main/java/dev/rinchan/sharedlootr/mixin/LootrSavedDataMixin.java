package dev.rinchan.sharedlootr.mixin;

import java.util.Map;
import java.util.UUID;
import noobanidus.mods.lootr.common.data.LootrInventory;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootrSavedData.class)
public abstract class LootrSavedDataMixin {
    private static final UUID GLOBAL_INVENTORY_OWNER = new UUID(0L, 1L);

    @Shadow(remap = false)
    private Map<UUID, LootrInventory> inventories;

    @Inject(
            method = "getInventory(Ljava/util/UUID;)Lnoobanidus/mods/lootr/common/data/LootrInventory;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sharedLootr$getGlobalInventory(UUID ignoredPlayerId, CallbackInfoReturnable<LootrInventory> cir) {
        Map.Entry<UUID, LootrInventory> canonical = selectCanonicalInventory();
        if (canonical == null) {
            return;
        }
        UUID owner = canonical.getKey();
        LootrInventory inventory = canonical.getValue();
        if (inventories.size() > 1) {
            inventories.clear();
            inventories.put(owner, inventory);
            ((LootrSavedData) (Object) this).markChanged();
        }
        inventory.setInfo((LootrSavedData) (Object) this);
        cir.setReturnValue(inventory);
    }

    private Map.Entry<UUID, LootrInventory> selectCanonicalInventory() {
        Map.Entry<UUID, LootrInventory> selected = null;
        int selectedOccupiedSlots = -1;
        for (Map.Entry<UUID, LootrInventory> candidate : inventories.entrySet()) {
            int occupiedSlots = occupiedSlots(candidate.getValue());
            if (selected == null
                    || occupiedSlots > selectedOccupiedSlots
                    || (occupiedSlots == selectedOccupiedSlots && preferredOwner(candidate.getKey(), selected.getKey()))) {
                selected = candidate;
                selectedOccupiedSlots = occupiedSlots;
            }
        }
        return selected;
    }

    private static int occupiedSlots(LootrInventory inventory) {
        int occupied = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    private static boolean preferredOwner(UUID candidate, UUID selected) {
        if (candidate.equals(GLOBAL_INVENTORY_OWNER)) {
            return true;
        }
        if (selected.equals(GLOBAL_INVENTORY_OWNER)) {
            return false;
        }
        return candidate.compareTo(selected) < 0;
    }
}
