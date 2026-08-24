package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.state.SharedSavedDataTruth;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootrSavedData.class, remap = false)
public abstract class SavedDataOpenedStateMixin {
    @Inject(method = "hasBeenOpened", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void sharedLootr$deriveOpenedFromSharedInventory(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SharedSavedDataTruth.exists((LootrSavedData) (Object) this));
    }
}
