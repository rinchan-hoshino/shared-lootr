package dev.rinchan.sharedlootr.mixin;

import dev.rinchan.rinlib.state.SharedOwnerState;
import noobanidus.mods.lootr.data.ChestData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ChestData.class, remap = false)
public abstract class NeoForgeChestDataMixin {
    private static final String MAP_PUT = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";

    @Redirect(
            method = "getInventory(Lnet/minecraft/server/level/ServerPlayer;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1
    )
    private Object sharedLootr$readSharedInventory(Map<UUID, ?> inventories, Object ignoredOwner) {
        return SharedOwnerState.get(inventories);
    }

    @Redirect(
            method = "clearInventory(Ljava/util/UUID;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 1
    )
    private Object sharedLootr$clearSharedInventory(Map<UUID, ?> inventories, Object ignoredOwner) {
        return SharedOwnerState.remove(inventories);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = MAP_PUT),
            require = 1
    )
    private Object sharedLootr$writeSizedInventory(Map<?, ?> inventories, Object ignoredOwner, Object inventory) {
        return putShared(inventories, inventory);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = MAP_PUT),
            require = 1
    )
    private Object sharedLootr$writeBlockInventory(Map<?, ?> inventories, Object ignoredOwner, Object inventory) {
        return putShared(inventories, inventory);
    }

    @Redirect(
            method = "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            at = @At(value = "INVOKE", target = MAP_PUT),
            require = 1
    )
    private Object sharedLootr$writeRandomizableInventory(Map<?, ?> inventories, Object ignoredOwner, Object inventory) {
        return putShared(inventories, inventory);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object putShared(Map inventories, Object inventory) {
        return inventories.put(SharedOwnerState.OWNER, inventory);
    }
}
