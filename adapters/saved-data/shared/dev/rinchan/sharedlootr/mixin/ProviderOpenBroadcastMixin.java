package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.state.SharedSavedDataTruth;
import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.impl.DefaultLootrAPIImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DefaultLootrAPIImpl.class, remap = false)
public abstract class ProviderOpenBroadcastMixin {
    @Redirect(
            method = "handleProviderOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/MenuBuilder;)V",
            at = @At(value = "INVOKE", target = "Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;performOpen(Lnet/minecraft/server/level/ServerPlayer;)V", remap = false),
            require = 1,
            remap = false
    )
    private void sharedLootr$broadcastSharedOpen(ILootrInfoProvider provider, ServerPlayer ignoredPlayer) {
        if (SharedSavedDataTruth.exists(provider)) {
            provider.performOpen();
        }
    }
}
