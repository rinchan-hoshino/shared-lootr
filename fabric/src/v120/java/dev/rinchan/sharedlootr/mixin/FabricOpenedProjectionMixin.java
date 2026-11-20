package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.zestyblaze.lootr.util.ChestUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(value = ChestUtil.class, remap = false)
public abstract class FabricOpenedProjectionMixin {
    @ModifyArg(
            method = "addOpener",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false),
            index = 0,
            require = 1,
            remap = false
    )
    private static Object sharedLootr$projectSharedOpened(Object owner) {
        return PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null);
    }
}
