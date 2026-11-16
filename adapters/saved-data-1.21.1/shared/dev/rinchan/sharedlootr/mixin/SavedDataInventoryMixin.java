package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.common.data.LootrSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

/**
 * Normalizes direct UUID-based clear calls so player-scoped clears still target the shared inventory.
 */
@Mixin(value = LootrSavedData.class, remap = false)
public abstract class SavedDataInventoryMixin {
    @ModifyVariable(method = "clearInventories(Ljava/util/UUID;)Z", at = @At("HEAD"), argsOnly = true, require = 1, remap = false)
    private UUID sharedLootr$normalizeClearOwner(UUID ignoredOwner) {
        return SharedOwnerState.OWNER;
    }
}
