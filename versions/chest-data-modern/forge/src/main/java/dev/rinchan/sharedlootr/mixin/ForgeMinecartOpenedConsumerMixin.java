package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootrChestMinecartEntity.class)
public abstract class ForgeMinecartOpenedConsumerMixin {
    @ModifyArg(
        method = "startSeenByPlayer",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false),
        index = 0,
        require = 1
    )
    private Object sharedLootr$readSharedOpened(Object ignoredPlayer) {
        return SharedOwnerState.OWNER;
    }
}
