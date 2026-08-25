package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import net.zestyblaze.lootr.util.ChestUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ChestUtil.class, remap = false)
public abstract class FabricOpenedProjectionMixin {
    @ModifyArg(
        method = {"handleLootChest", "handleLootInventory"},
        at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false),
        index = 0,
        require = 2,
        remap = false
    )
    private static Object sharedLootr$projectSharedOpened(Object ignoredPlayer) {
        return SharedOwnerState.OWNER;
    }
}
