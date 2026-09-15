package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.block.tile.LootrBarrelTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(value = LootrBarrelTileEntity.class, remap = false)
abstract class ForgeBarrelOpenedConsumerMixin {
    @ModifyArg(method = "getModelData", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"), index = 0, require = 1, remap = false)
    private Object sharedLootr$readSharedOpenedState(Object ignoredPlayerOwner) {
        return SharedOwnerState.OWNER;
    }
}
