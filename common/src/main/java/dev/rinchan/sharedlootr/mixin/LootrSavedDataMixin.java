package dev.rinchan.sharedlootr.mixin;

import java.util.Map;
import java.util.UUID;
import noobanidus.mods.lootr.common.data.LootrInventory;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
        LootrInventory inventory = inventories.get(GLOBAL_INVENTORY_OWNER);
        if (inventory == null) {
            Map.Entry<UUID, LootrInventory> legacy = selectCanonicalInventory();
            if (legacy == null) {
                return;
            }
            inventory = legacy.getValue();
            inventories.put(GLOBAL_INVENTORY_OWNER, inventory);
            ((LootrSavedData) (Object) this).markChanged();
        }
        inventory.setInfo((LootrSavedData) (Object) this);
        cir.setReturnValue(inventory);
    }

    @ModifyArg(
            method = "createInventory(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/data/LootFiller;)Lnoobanidus/mods/lootr/common/data/LootrInventory;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            index = 0,
            remap = false
    )
    private Object sharedLootr$storeNewInventoryGlobally(Object ignoredPlayerId) {
        return GLOBAL_INVENTORY_OWNER;
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
