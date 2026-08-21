package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.marker.LootChestMarker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity.class)
abstract class RandomizableContainerBlockEntityMixin {
    @Shadow
    protected ResourceKey<LootTable> lootTable;

    @Inject(method = "createMenu", at = @At("HEAD"))
    private void sharedlootr$rememberVanillaLootChest(
        int containerId,
        Inventory inventory,
        Player player,
        CallbackInfoReturnable<AbstractContainerMenu> callback
    ) {
        if (lootTable != null && (Object) this instanceof ChestBlockEntity chest) {
            ((LootChestMarker) chest).wmf$markAsLootChest();
        }
    }
}
