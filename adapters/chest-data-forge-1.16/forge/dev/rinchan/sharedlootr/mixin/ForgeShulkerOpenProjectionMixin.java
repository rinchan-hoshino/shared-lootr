package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.block.tile.LootrShulkerTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootrShulkerTileEntity.class)
abstract class ForgeShulkerOpenProjectionMixin {
    @ModifyArg(method = "stopOpen", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false), index = 0, require = 1)
    private Object sharedLootr$projectSharedOpened(Object ignoredPlayerOwner) {
        return SharedOwnerState.OWNER;
    }
}
