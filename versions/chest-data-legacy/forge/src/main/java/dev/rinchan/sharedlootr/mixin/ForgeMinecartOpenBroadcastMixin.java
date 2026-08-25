package dev.rinchan.sharedlootr.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.network.PacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootrChestMinecartEntity.class)
public abstract class ForgeMinecartOpenBroadcastMixin {
    @Redirect(
        method = {"startOpen", "m_5856_"},
        at = @At(
            value = "INVOKE",
            target = "Lnoobanidus/mods/lootr/network/PacketHandler;sendToInternal(Ljava/lang/Object;Lnet/minecraft/server/level/ServerPlayer;)V",
            remap = false
        ),
        require = 1,
        remap = false
    )
    private void sharedLootr$broadcastOpened(Object packet, ServerPlayer ignoredPlayer) {
        PacketHandler.sendInternal(
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> (Entity) (Object) this),
            packet
        );
    }
}
