package dev.rinchan.sharedlootr.mixin;

import net.minecraft.resources.Identifier;
import noobanidus.mods.lootr.common.api.LootrAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Enables Lootr team sharing out of the box and pins Shared Lootr's team resolver.
 */
@Mixin(value = LootrAPI.class, remap = false)
public abstract class LootrTeamConfigMixin {
    private static final Identifier SHARED_RESOLVER_ID = Identifier.fromNamespaceAndPath("shared_lootr", "shared");

    @Inject(method = "isTeamLoot", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void sharedLootr$enableTeamLoot(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "getPinnedTeamResolver", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void sharedLootr$pinSharedResolver(CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(SHARED_RESOLVER_ID);
    }
}
