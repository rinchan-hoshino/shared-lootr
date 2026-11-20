package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.zestyblaze.lootr.data.ChestData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ChestData.class, remap = false)
public abstract class FabricChestDataMixin {
    @Redirect(
            method = "getInventory*",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1
    )
    private Object sharedLootr$readSharedInventory(Map<UUID, ?> inventories, Object owner) {
        return inventories.get(PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null));
    }

    @Redirect(
            method = "clearInventory(Ljava/util/UUID;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1
    )
    private Object sharedLootr$clearSharedInventory(Map<UUID, ?> inventories, Object owner) {
        return inventories.remove(PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null));
    }

    @Redirect(
            method = "createInventory*",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 3
    )
    private Object sharedLootr$writeSharedInventory(Map<UUID, Object> inventories, Object owner, Object inventory) {
        return inventories.put(PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null), inventory);
    }
}
