package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import noobanidus.mods.lootr.data.ChestData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ChestData.class, remap = false)
public abstract class ForgeChestDataMixin {
    private static final String MAP_PUT = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";

    @Redirect(
            method = "getInventory(Ljava/util/UUID;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1
    )
    private Object sharedLootr$readSharedInventory(Map<UUID, ?> inventories, Object owner) {
        return inventories.get(PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null));
    }

    @Redirect(
            method = "clearInventory(Ljava/util/UUID;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1
    )
    private Object sharedLootr$clearSharedInventory(Map<UUID, ?> inventories, Object owner) {
        return inventories.remove(PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null));
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = MAP_PUT),
            require = 1
    )
    private Object sharedLootr$writeSizedInventory(Map<UUID, Object> inventories, Object owner, Object inventory) {
        return putResolved(inventories, owner, inventory);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = MAP_PUT),
            require = 1
    )
    private Object sharedLootr$writeBlockInventory(Map<UUID, Object> inventories, Object owner, Object inventory) {
        return putResolved(inventories, owner, inventory);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = MAP_PUT),
            require = 1
    )
    private Object sharedLootr$writeRandomizableInventory(Map<UUID, Object> inventories, Object owner, Object inventory) {
        return putResolved(inventories, owner, inventory);
    }

    private static Object putResolved(Map<UUID, Object> inventories, Object owner, Object inventory) {
        return inventories.put(PlayerPoolResolver.resolveServerByUuid(owner instanceof UUID uuid ? uuid : null), inventory);
    }
}
