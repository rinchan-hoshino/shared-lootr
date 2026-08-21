package dev.rinchan.sharedlootr.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrBrushableBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrDecoratedPotBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrShulkerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
    LootrChestBlockEntity.class,
    LootrBarrelBlockEntity.class,
    LootrShulkerBlockEntity.class,
    LootrDecoratedPotBlockEntity.class,
    LootrBrushableBlockEntity.class
})
public abstract class LootrBlockEntityMixin {
    @Inject(method = "markChanged", at = @At("TAIL"), remap = false)
    private void sharedLootr$broadcastOpenedState(CallbackInfo ci) {
        ILootrInfoProvider provider = (ILootrInfoProvider) (Object) this;
        Level level = provider.getInfoLevel();
        if (level == null || level.isClientSide() || !provider.hasBeenOpened()) {
            return;
        }

        BlockPos position = provider.getInfoPos();
        BlockState state = level.getBlockState(position);
        level.sendBlockUpdated(position, state, state, Block.UPDATE_CLIENTS);
    }
}
