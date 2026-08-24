package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.StickyBoolean;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import noobanidus.mods.lootr.common.api.NBTConstants;
import noobanidus.mods.lootr.common.api.data.SimpleLootrInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleLootrInstance.class)
public abstract class SimpleLootrInstanceMixin {
    @Shadow(remap = false)
    protected boolean clientOpened;

    @ModifyVariable(method = "setClientOpened", at = @At("HEAD"), argsOnly = true, remap = false)
    private boolean sharedLootr$keepGlobalOpenedVisual(boolean opened) {
        return StickyBoolean.next(clientOpened, opened);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void sharedLootr$useOpenedFlagAsClientMarker(
            CompoundTag tag,
            HolderLookup.Provider provider,
            CallbackInfo callback
    ) {
        if (tag.contains(NBTConstants.HAS_BEEN_OPENED, Tag.TAG_BYTE)) {
            clientOpened = StickyBoolean.next(clientOpened, tag.getBoolean(NBTConstants.HAS_BEEN_OPENED));
        }
    }
}
