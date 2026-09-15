package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.state.SharedInventoryTruth;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.helper.SimpleLootrInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SimpleLootrInstance.class, remap = false)
abstract class UpdateTagProjectionMixin {
    @Shadow
    protected boolean hasBeenOpened;

    @Inject(method = "fillUpdateTag", at = @At("HEAD"), require = 1, remap = false)
    private void sharedLootr$projectServerTruth(
            HolderLookup.Provider provider,
            boolean clientSide,
            BlockEntity parent,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        if (!clientSide && parent instanceof ILootrContainerInstance instance) {
            hasBeenOpened = SharedInventoryTruth.exists(instance);
        }
    }
}
