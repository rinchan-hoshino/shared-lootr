package dev.rinchan.sharedlootr.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.impl.DefaultLootrAPIImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultLootrAPIImpl.class)
public abstract class DefaultLootrAPIImplMixin {
    @Redirect(
            method = "handleProviderOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/MenuBuilder;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;addOpener(Lnet/minecraft/world/entity/player/Player;)Z"
            ),
            remap = false
    )
    private boolean sharedLootr$broadcastReopen(
            ILootrInfoProvider provider,
            Player player
    ) {
        boolean added = provider.addOpener(player);
        if (!added) {
            provider.performOpen();
        }
        return added;
    }

    @Redirect(
            method = "handleProviderOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/MenuBuilder;)V",
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
