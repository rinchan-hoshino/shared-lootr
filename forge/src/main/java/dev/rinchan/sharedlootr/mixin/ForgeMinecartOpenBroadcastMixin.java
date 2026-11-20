package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.network.PacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(LootrChestMinecartEntity.class)
public abstract class ForgeMinecartOpenBroadcastMixin {
    @Redirect(
            method = {"startSeenByPlayer", "m_6457_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnoobanidus/mods/lootr/network/PacketHandler;sendToInternal(Ljava/lang/Object;Lnet/minecraft/server/level/ServerPlayer;)V",
                    remap = false
            ),
            require = 1,
            remap = false
    )
    private void sharedLootr$broadcastOpened(Object packet, ServerPlayer opener) {
        if (SharedLootrConfig.get().sharingMode() == SharingMode.GLOBAL) {
            PacketHandler.sendInternal(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> (Entity) (Object) this),
                    packet
            );
            return;
        }
        UUID openerPool = PlayerPoolResolver.resolveServer(opener);
        if (opener.level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                if (openerPool.equals(PlayerPoolResolver.resolveServer(player))) {
                    PacketHandler.sendToInternal(packet, player);
                }
            }
        } else {
            PacketHandler.sendToInternal(packet, opener);
        }
    }
}
