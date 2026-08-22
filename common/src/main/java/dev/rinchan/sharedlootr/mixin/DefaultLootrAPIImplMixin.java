package dev.rinchan.sharedlootr.mixin;

import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.impl.DefaultLootrAPIImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultLootrAPIImpl.class)
public abstract class DefaultLootrAPIImplMixin {
    @Redirect(
            method = "handleProviderOpen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;performOpen(Lnet/minecraft/server/level/ServerPlayer;)V"
            ),
            remap = false
    )
    private void sharedLootr$broadcastGlobalOpen(
            ILootrInfoProvider provider,
            ServerPlayer ignoredPlayer
    ) {
        provider.performOpen();
    }
}
