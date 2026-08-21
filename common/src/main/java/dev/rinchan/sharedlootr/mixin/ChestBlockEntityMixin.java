package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.marker.LootChestMarker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
abstract class ChestBlockEntityMixin implements LootChestMarker {
    @Unique
    private static final String SHAREDLOOTR_LOOT_CHEST_TAG = "wmf_loot_chest";
    @Unique
    private static final String SHAREDLOOTR_OPENED_TAG = "wmf_loot_chest_opened";

    @Unique
    private boolean sharedlootr$lootChest;
    @Unique
    private boolean sharedlootr$opened;

    @Override
    public boolean wmf$isLootChest() {
        return sharedlootr$lootChest;
    }

    @Override
    public boolean wmf$wasOpened() {
        return sharedlootr$opened;
    }

    @Override
    public void wmf$markAsLootChest() {
        sharedlootr$lootChest = true;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void sharedlootr$loadMarker(
        CompoundTag tag,
        HolderLookup.Provider registries,
        CallbackInfo callback
    ) {
        sharedlootr$lootChest = tag.getBoolean(SHAREDLOOTR_LOOT_CHEST_TAG);
        sharedlootr$opened = tag.getBoolean(SHAREDLOOTR_OPENED_TAG);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void sharedlootr$saveMarker(
        CompoundTag tag,
        HolderLookup.Provider registries,
        CallbackInfo callback
    ) {
        tag.putBoolean(SHAREDLOOTR_LOOT_CHEST_TAG, sharedlootr$lootChest);
        tag.putBoolean(SHAREDLOOTR_OPENED_TAG, sharedlootr$opened);
    }

    @Inject(method = "startOpen", at = @At("HEAD"))
    private void sharedlootr$markOpened(Player player, CallbackInfo callback) {
        ChestBlockEntity chest = (ChestBlockEntity) (Object) this;
        Level level = chest.getLevel();
        if (level == null || level.isClientSide() || !sharedlootr$lootChest || sharedlootr$opened) {
            return;
        }
        sharedlootr$opened = true;
        chest.setChanged();
        level.sendBlockUpdated(chest.getBlockPos(), chest.getBlockState(), chest.getBlockState(), 3);
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity) (Object) this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return ((BlockEntity) (Object) this).saveWithoutMetadata(registries);
    }
}
