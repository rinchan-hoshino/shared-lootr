package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.client.block.LootrShulkerBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootrShulkerBlockRenderer.class)
public abstract class ForgeShulkerOpenedConsumerMixin {
    @ModifyArg(
        method = "getMaterial",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false),
        index = 0,
        require = 1,
        remap = false
    )
    private Object sharedLootr$readSharedOpened(Object ignoredPlayer) {
        return SharedOwnerState.OWNER;
    }
}
