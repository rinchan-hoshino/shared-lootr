package dev.rinchan.sharedlootr.mixin.client;

import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.minecraft.client.Minecraft;
import noobanidus.mods.lootr.client.block.LootrChestBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootrChestBlockRenderer.class)
public abstract class ForgeChestOpenedConsumerMixin {
    @ModifyArg(
            method = "getMaterial",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false),
            index = 0,
            require = 1,
            remap = false
    )
    private Object sharedLootr$readSharedOpened(Object ignoredPlayer) {
        return PlayerPoolResolver.resolveClient(Minecraft.getInstance().player);
    }
}
