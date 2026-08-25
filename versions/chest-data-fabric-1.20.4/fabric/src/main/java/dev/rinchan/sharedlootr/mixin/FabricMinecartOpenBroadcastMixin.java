package dev.rinchan.sharedlootr.mixin;

import java.util.LinkedHashSet;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.zestyblaze.lootr.entity.LootrChestMinecartEntity;
import net.zestyblaze.lootr.network.NetworkConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootrChestMinecartEntity.class)
public abstract class FabricMinecartOpenBroadcastMixin {
    @Redirect(
        method = "startOpen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/zestyblaze/lootr/network/NetworkConstants;sendOpenCart(ILnet/minecraft/server/level/ServerPlayer;)V"
        ),
        require = 1
    )
    private void sharedLootr$broadcastOpened(int entityId, ServerPlayer opener) {
        LinkedHashSet<ServerPlayer> recipients = new LinkedHashSet<>(
            PlayerLookup.tracking((Entity) (Object) this)
        );
        recipients.add(opener);
        for (ServerPlayer recipient : recipients) {
            NetworkConstants.sendOpenCart(entityId, recipient);
        }
    }
}
