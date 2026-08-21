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
        LootrInventory global = inventories.get(GLOBAL_INVENTORY_OWNER);
        if (global == null && !inventories.isEmpty()) {
            global = inventories.values().iterator().next();
            inventories.clear();
            inventories.put(GLOBAL_INVENTORY_OWNER, global);
            ((LootrSavedData) (Object) this).markChanged();
        }
        if (global != null) {
            global.setInfo((LootrSavedData) (Object) this);
            cir.setReturnValue(global);
        }
    }

    @ModifyArg(
            method = {
                    "createInventory(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/data/LootFiller;)Lnoobanidus/mods/lootr/common/data/LootrInventory;",
                    "createInventory(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Ljava/util/UUID;Lnoobanidus/mods/lootr/common/api/data/LootFiller;)Lnoobanidus/mods/lootr/common/data/LootrInventory;"
            },
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            index = 0,
            remap = false
    )
    private Object sharedLootr$storeInventoryOnce(Object ignoredPlayerId) {
        return GLOBAL_INVENTORY_OWNER;
    }
}
