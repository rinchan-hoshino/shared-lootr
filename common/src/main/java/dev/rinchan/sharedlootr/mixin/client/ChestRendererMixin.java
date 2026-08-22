package dev.rinchan.sharedlootr.mixin.client;

import dev.rinchan.sharedlootr.marker.LootChestMarker;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.neoforge.client.block.LootrChestBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestRenderer.class)
abstract class ChestRendererMixin<T extends BlockEntity & LidBlockEntity> {
    @Inject(method = "getMaterial", at = @At("HEAD"), cancellable = true)
    private void sharedlootr$useLootrMaterial(
        T chest,
        ChestType chestType,
        CallbackInfoReturnable<Material> callback
    ) {
        if (!(chest instanceof LootChestMarker marker)
            || !marker.wmf$isLootChest()
            || LootrAPI.isVanillaTextures()) {
            return;
        }

        boolean trapped = chest.getBlockState().is(Blocks.TRAPPED_CHEST);
        boolean opened = marker.wmf$wasOpened();
        if (LootrAPI.isOldTextures()) {
            callback.setReturnValue(trapped
                ? (opened ? LootrChestBlockRenderer.OLD_MATERIAL4 : LootrChestBlockRenderer.OLD_MATERIAL3)
                : (opened ? LootrChestBlockRenderer.OLD_MATERIAL2 : LootrChestBlockRenderer.OLD_MATERIAL));
        } else {
            callback.setReturnValue(trapped
                ? (opened ? LootrChestBlockRenderer.MATERIAL4 : LootrChestBlockRenderer.MATERIAL3)
                : (opened ? LootrChestBlockRenderer.MATERIAL2 : LootrChestBlockRenderer.MATERIAL));
        }
    }
}
