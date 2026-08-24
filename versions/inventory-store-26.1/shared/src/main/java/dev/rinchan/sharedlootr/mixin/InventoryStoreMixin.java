package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.common.data.LootrInventoryStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = LootrInventoryStore.class, remap = false)
abstract class InventoryStoreMixin {
    @Redirect(
            method = "getInventory",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1,
            remap = false
    )
    private Object sharedLootr$get(Map<UUID, ?> inventories, Object ignoredOwner) {
        return SharedOwnerState.get(inventories);
    }

    @Redirect(
            method = "createInventory",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1,
            remap = false
    )
    private Object sharedLootr$put(Map<UUID, Object> inventories, Object ignoredOwner, Object inventory) {
        return SharedOwnerState.put(inventories, inventory);
    }

    @Redirect(
            method = "clearInventories(Ljava/util/UUID;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1,
            remap = false
    )
    private Object sharedLootr$remove(Map<UUID, ?> inventories, Object ignoredOwner) {
        return inventories.remove(SharedOwnerState.OWNER);
    }
}
