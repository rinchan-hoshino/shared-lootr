package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = LootrSavedData.class, remap = false)
public abstract class SavedDataInventoryMixin {
    private static final String MAP_GET = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String MAP_PUT = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String MAP_REMOVE = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;";

    @Redirect(method = "getInventory*", at = @At(value = "INVOKE", target = MAP_GET, remap = false), require = 1, remap = false)
    private Object sharedLootr$getShared(Map<UUID, ?> inventories, Object ignoredOwner) {
        return SharedOwnerState.get(inventories);
    }

    @Redirect(method = "createInventory*", at = @At(value = "INVOKE", target = MAP_PUT, remap = false), require = 2, remap = false)
    private Object sharedLootr$putShared(Map<UUID, Object> inventories, Object ignoredOwner, Object inventory) {
        return SharedOwnerState.put(inventories, inventory);
    }

    @Redirect(method = "clearInventories", at = @At(value = "INVOKE", target = MAP_REMOVE, remap = false), require = 1, remap = false)
    private Object sharedLootr$removeShared(Map<UUID, ?> inventories, Object ignoredOwner) {
        return inventories.remove(SharedOwnerState.OWNER);
    }
}
