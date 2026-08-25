package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = LootrChestMinecartEntity.class, remap = false)
abstract class ForgeMinecartOpenBroadcastMixin {
    @ModifyArg(method = "addOpener", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"), index = 0, require = 1, remap = false)
    private Object sharedLootr$broadcastOpenedState(Object ignoredPlayerOwner) {
        return SharedOwnerState.OWNER;
    }
}
