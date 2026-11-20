package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(LootrChestMinecartEntity.class)
public abstract class ForgeMinecartOpenedConsumerMixin {
    @ModifyArg(
            method = {"startSeenByPlayer", "m_6457_"},
            at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false),
            index = 0,
            require = 1
    )
    private Object sharedLootr$readSharedOpened(Object owner) {
        return PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null);
    }
}
