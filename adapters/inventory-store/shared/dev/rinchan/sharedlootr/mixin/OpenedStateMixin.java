package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.state.SharedInventoryTruth;
import net.minecraft.world.level.Level;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
        "noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity",
        "noobanidus.mods.lootr.common.block.entity.LootrBrushableBlockEntity",
        "noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity",
        "noobanidus.mods.lootr.common.block.entity.LootrDecoratedPotBlockEntity",
        "noobanidus.mods.lootr.common.block.entity.LootrShulkerBoxBlockEntity",
        "noobanidus.mods.lootr.common.entity.LootrChestMinecartEntity",
        "noobanidus.mods.lootr.common.entity.LootrItemFrame"
}, remap = false)
abstract class OpenedStateMixin {
    @Inject(method = "hasBeenOpened", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void sharedLootr$serverTruth(CallbackInfoReturnable<Boolean> cir) {
        ILootrContainerInstance instance = (ILootrContainerInstance) (Object) this;
        Level level = instance.getDataLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        cir.setReturnValue(SharedInventoryTruth.exists(instance));
    }
}
