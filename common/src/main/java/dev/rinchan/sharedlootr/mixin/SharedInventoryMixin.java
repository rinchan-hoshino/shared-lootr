package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.state.SharedInventoryState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.UUID;

@Mixin(targets = "noobanidus.mods.lootr.common.data.LootrSavedData", remap = false)
public abstract class SharedInventoryMixin implements SharedInventoryState {
    @Shadow
    private Map<UUID, Object> inventories;

    @ModifyArg(
            method = "getInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            ),
            index = 0,
            require = 0,
            remap = false
    )
    private Object sharedLootr$readSharedInventory(Object ignoredOwner) {
        return SharedOwnerState.OWNER;
    }

    @ModifyArg(
            method = {"createInventory", "createInventoryRaw"},
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            ),
            index = 0,
            require = 0,
            remap = false
    )
    private Object sharedLootr$writeSharedInventory(Object ignoredOwner) {
        return SharedOwnerState.OWNER;
    }

    @Override
    public boolean sharedLootr$hasSharedInventory() {
        return SharedOwnerState.contains(inventories);
    }
}
