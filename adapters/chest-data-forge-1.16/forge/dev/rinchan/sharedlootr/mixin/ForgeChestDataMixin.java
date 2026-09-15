package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.data.ChestData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ChestData.class, remap = false)
abstract class ForgeChestDataMixin {
    @Redirect(
            method = "getInventory",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1,
            remap = false
    )
    private Object sharedLootr$readSharedInventory(Map<UUID, Object> inventories, Object ignoredPlayerOwner) {
        return SharedOwnerState.get(inventories);
    }

    @Redirect(
            method = "createInventory",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1,
            remap = false
    )
    private Object sharedLootr$writeSharedInventory(Map<UUID, Object> inventories, Object ignoredPlayerOwner, Object inventory) {
        SharedOwnerState.put(inventories, inventory);
        return null;
    }

    @Redirect(
            method = "clearInventory",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1,
            remap = false
    )
    private Object sharedLootr$clearSharedInventory(Map<UUID, Object> inventories, Object ignoredPlayerOwner) {
        return SharedOwnerState.remove(inventories);
    }
}
