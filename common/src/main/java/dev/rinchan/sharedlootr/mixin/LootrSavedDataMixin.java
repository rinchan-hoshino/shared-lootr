package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.state.SharedInventoryState;
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
public abstract class LootrSavedDataMixin implements SharedInventoryState {
    @Shadow(remap = false)
    private Map<UUID, LootrInventory> inventories;

    @Inject(
            method = "getInventory(Ljava/util/UUID;)Lnoobanidus/mods/lootr/common/data/LootrInventory;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sharedLootr$getGlobalInventory(UUID ignoredPlayerId, CallbackInfoReturnable<LootrInventory> cir) {
        LootrInventory inventory = SharedOwnerState.get(inventories);
        if (inventory != null) {
            inventory.setInfo((LootrSavedData) (Object) this);
        }
        cir.setReturnValue(inventory);
    }

    @ModifyArg(
            method = "createInventory(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/data/LootFiller;)Lnoobanidus/mods/lootr/common/data/LootrInventory;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            index = 0,
            remap = false
    )
    private Object sharedLootr$storeNewInventoryGlobally(Object ignoredPlayerId) {
        return SharedOwnerState.OWNER;
    }

    @Override
    public boolean sharedLootr$hasSharedInventory() {
        return SharedOwnerState.contains(inventories);
    }
}
