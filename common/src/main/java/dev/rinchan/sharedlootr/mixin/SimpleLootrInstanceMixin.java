package dev.rinchan.sharedlootr.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import noobanidus.mods.lootr.common.api.NBTConstants;
import noobanidus.mods.lootr.common.api.data.SimpleLootrInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleLootrInstance.class)
public abstract class SimpleLootrInstanceMixin {
    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void sharedLootr$useOpenedFlagAsClientMarker(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (tag.contains(NBTConstants.HAS_BEEN_OPENED, Tag.TAG_BYTE)) {
            ((SimpleLootrInstance) (Object) this).setClientOpened(tag.getBoolean(NBTConstants.HAS_BEEN_OPENED));
        }
    }
}
