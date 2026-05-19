package dev.rinchan.sharedlootr.mixin;

import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.LootFiller;
import noobanidus.mods.lootr.common.data.LootrInventory;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootrSavedData.class)
public abstract class LootrSavedDataMixin {
    private static final UUID SHARED_LOOTR_INVENTORY_ID = new UUID(0L, 1L);

    @Shadow(remap = false)
    private Map<UUID, LootrInventory> inventories;

    @Inject(method = "getInventory(Ljava/util/UUID;)Lnoobanidus/mods/lootr/common/data/LootrInventory;", at = @At("HEAD"), cancellable = true, remap = false)
    private void sharedLootr$getSharedInventory(UUID playerId, CallbackInfoReturnable<LootrInventory> cir) {
        LootrInventory shared = inventories.get(SHARED_LOOTR_INVENTORY_ID);
        if (shared == null && playerId != null) {
            shared = inventories.get(playerId);
            if (shared != null) {
                inventories.put(SHARED_LOOTR_INVENTORY_ID, shared);
            }
        }
        if (shared != null) {
            shared.setInfo((LootrSavedData) (Object) this);
            cir.setReturnValue(shared);
        }
    }

    @Inject(method = "createInventory(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/data/LootFiller;)Lnoobanidus/mods/lootr/common/data/LootrInventory;", at = @At("RETURN"), remap = false)
    private void sharedLootr$storeCreatedInventoryAsShared(ILootrInfoProvider provider, ServerPlayer player, LootFiller filler, CallbackInfoReturnable<LootrInventory> cir) {
        LootrInventory created = cir.getReturnValue();
        if (created != null) {
            inventories.put(SHARED_LOOTR_INVENTORY_ID, created);
        }
    }
}
