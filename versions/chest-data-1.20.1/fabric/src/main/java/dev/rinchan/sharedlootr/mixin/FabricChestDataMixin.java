package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
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
    private Object sharedLootr$readSharedInventory(Map<UUID, ?> inventories, Object ignoredOwner) {
        return SharedOwnerState.get(inventories);
    }

    @Redirect(
            method = "createInventory*",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 3
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object sharedLootr$writeSharedInventory(Map inventories, Object ignoredOwner, Object inventory) {
        return inventories.put(SharedOwnerState.OWNER, inventory);
    }
}
