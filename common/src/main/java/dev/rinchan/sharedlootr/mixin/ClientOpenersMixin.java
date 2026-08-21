package dev.rinchan.sharedlootr.mixin;

import java.util.UUID;
import noobanidus.mods.lootr.common.api.IClientOpeners;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IClientOpeners.class)
public interface ClientOpenersMixin {
    @Inject(method = "hasClientOpened(Ljava/util/UUID;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void sharedLootr$useGlobalOpenedMarker(UUID ignoredPlayerId, CallbackInfoReturnable<Boolean> cir) {
        IClientOpeners self = (IClientOpeners) (Object) this;
        cir.setReturnValue(self.isClientOpened());
    }
}
