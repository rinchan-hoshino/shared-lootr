package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.client.LootrChestBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = LootrChestBlockRenderer.class, remap = false)
abstract class ForgeChestOpenedConsumerMixin {
    @ModifyArg(method = "getMaterial", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"), index = 0, require = 1, remap = false)
    private Object sharedLootr$readSharedOpenedState(Object ignoredPlayerOwner) {
        return SharedOwnerState.OWNER;
    }
}
