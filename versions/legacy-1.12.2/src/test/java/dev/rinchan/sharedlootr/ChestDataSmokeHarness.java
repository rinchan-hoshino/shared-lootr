package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public final class ChestDataSmokeHarness {
    private ChestDataSmokeHarness() {
    }

    public static void run(MinecraftServer server) {
        try {
            Class<?> dataClass = Class.forName("noobanidus.mods.lootr.data.ChestData");
            Object data = dataClass.getConstructor(String.class).newInstance("shared_lootr_smoke");
            Field inventoriesField = dataClass.getDeclaredField("inventories");
            inventoriesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Object> inventories = (Map<UUID, Object>) inventoriesField.get(data);

            Class<?> inventoryClass = Class.forName("noobanidus.mods.lootr.data.SpecialChestInventory");
            Constructor<?> inventoryConstructor = null;
            for (Constructor<?> candidate : inventoryClass.getDeclaredConstructors()) {
                if (candidate.getParameterTypes().length > 1
                        && NonNullList.class.isAssignableFrom(candidate.getParameterTypes()[1])) {
                    inventoryConstructor = candidate;
                    break;
                }
            }
            if (inventoryConstructor == null) throw new IllegalStateException("Inventory constructor was not found");
            inventoryConstructor.setAccessible(true);
            Object[] inventoryArguments = new Object[inventoryConstructor.getParameterCount()];
            inventoryArguments[0] = data;
            inventoryArguments[1] = NonNullList.create();
            Object inventory = inventoryConstructor.newInstance(inventoryArguments);

            UUID legacyOwner = UUID.randomUUID();
            UUID otherPlayer = UUID.randomUUID();
            inventories.put(legacyOwner, inventory);

            Object world = server.getWorld(0);
            if (world == null) throw new IllegalStateException("WorldServer was not available");

            Class<?> playerClass = Class.forName("net.minecraft.entity.player.EntityPlayerMP");
            Object player = null;
            for (Constructor<?> constructor : playerClass.getDeclaredConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                Object[] arguments = new Object[parameters.length];
                boolean compatible = true;
                for (int index = 0; index < parameters.length; index++) {
                    String parameterName = parameters[index].getName();
                    if (MinecraftServer.class.isAssignableFrom(parameters[index])) {
                        arguments[index] = server;
                    } else if (parameterName.equals("net.minecraft.world.WorldServer")) {
                        arguments[index] = world;
                    } else if (parameters[index] == GameProfile.class) {
                        arguments[index] = new GameProfile(legacyOwner, "SharedLootrSmoke");
                    } else if (parameterName.endsWith("PlayerInteractionManager")) {
                        Constructor<?> manager = parameters[index].getDeclaredConstructors()[0];
                        manager.setAccessible(true);
                        arguments[index] = manager.newInstance(world);
                    } else {
                        compatible = false;
                    }
                }
                if (compatible) {
                    constructor.setAccessible(true);
                    player = constructor.newInstance(arguments);
                    break;
                }
            }
            if (player == null) throw new IllegalStateException("EntityPlayerMP constructor was not found");

            Method getInventory = dataClass.getDeclaredMethod("getInventory", playerClass);
            if (getInventory.invoke(data, player) != null) {
                throw new IllegalStateException("Legacy player inventory was still readable");
            }
            SharedOwnerState.put(inventories, inventory);
            if (getInventory.invoke(data, player) != inventory) {
                throw new IllegalStateException("Shared inventory was not returned");
            }
            Method clearInventory = dataClass.getDeclaredMethod("clearInventory", UUID.class);
            if (!Boolean.TRUE.equals(clearInventory.invoke(data, otherPlayer))) {
                throw new IllegalStateException("Shared inventory clear was not redirected");
            }
            if (SharedOwnerState.contains(inventories) || inventories.get(legacyOwner) != inventory) {
                throw new IllegalStateException("Clear did not preserve ignored legacy data");
            }

            Class.forName("noobanidus.mods.lootr.block.tile.LootrChestTileEntity");
            System.out.println("SHARED_LOOTR_1_12_SMOKE PASS");
        } catch (Throwable failure) {
            failure.printStackTrace();
            throw new RuntimeException("Shared Lootr 1.12 smoke failed", failure);
        }
    }
}
