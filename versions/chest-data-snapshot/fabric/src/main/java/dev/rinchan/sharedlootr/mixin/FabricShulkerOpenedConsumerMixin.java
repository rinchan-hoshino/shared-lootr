package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import net.zestyblaze.lootr.client.block.LootrShulkerBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootrShulkerBlockRenderer.class)
public abstract class FabricShulkerOpenedConsumerMixin {
    @ModifyArg(
        method = "getMaterial",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false),
        index = 0,
        require = 1
    )
    private Object sharedLootr$readSharedOpened(Object ignoredPlayer) {
        return SharedOwnerState.OWNER;
    }
}
