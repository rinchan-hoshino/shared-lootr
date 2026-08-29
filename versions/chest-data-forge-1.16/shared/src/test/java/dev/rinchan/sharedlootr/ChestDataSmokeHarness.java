package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public final class ChestDataSmokeHarness {
    private ChestDataSmokeHarness() {
    }

    public static void run(MinecraftServer server, String chestDataName, String chestUtilName) {
        try {
            Class<?> chestDataClass = Class.forName(chestDataName);
            Constructor<?> dataConstructor = chestDataClass.getDeclaredConstructor(String.class);
            dataConstructor.setAccessible(true);
            Object chestData = dataConstructor.newInstance("shared_lootr_smoke");

            Class<?> inventoryClass = Class.forName("noobanidus.mods.lootr.data.SpecialChestInventory");
            Constructor<?> inventoryConstructor = inventoryClass.getDeclaredConstructors()[0];
            inventoryConstructor.setAccessible(true);
            Object[] inventoryArguments = new Object[inventoryConstructor.getParameterCount()];
            inventoryArguments[0] = chestData;
            Object inventory = inventoryConstructor.newInstance(inventoryArguments);

            Field inventoriesField = chestDataClass.getDeclaredField("inventories");
            inventoriesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Object> inventories = (Map<UUID, Object>) inventoriesField.get(chestData);

            UUID legacyOwner = UUID.randomUUID();
            UUID otherPlayer = UUID.randomUUID();
            inventories.put(legacyOwner, inventory);

            Object world = null;
            for (Method method : server.getClass().getMethods()) {
                if (method.getParameterCount() == 0
                        && method.getReturnType().getName().equals("net.minecraft.world.server.ServerWorld")) {
                    world = method.invoke(server);
                    if (world != null) {
                        break;
                    }
                }
            }
            if (world == null) {
                throw new IllegalStateException("ServerWorld was not available");
            }
            Class<?> playerClass = Class.forName("net.minecraft.entity.player.ServerPlayerEntity");
            Object player = null;
            for (Constructor<?> constructor : playerClass.getDeclaredConstructors()) {
                Object[] arguments = new Object[constructor.getParameterCount()];
                Class<?>[] parameters = constructor.getParameterTypes();
                boolean compatible = true;
                for (int index = 0; index < parameters.length; index++) {
                    String name = parameters[index].getName();
                    if (MinecraftServer.class.isAssignableFrom(parameters[index])) {
                        arguments[index] = server;
                    } else if (name.equals("net.minecraft.world.server.ServerWorld")) {
                        arguments[index] = world;
                    } else if (parameters[index] == GameProfile.class) {
                        arguments[index] = new GameProfile(legacyOwner, "SharedLootrSmoke");
                    } else if (name.endsWith("PlayerInteractionManager")) {
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
            if (player == null) {
                throw new IllegalStateException("ServerPlayerEntity constructor was not found");
            }

            Method getInventory = chestDataClass.getDeclaredMethod("getInventory", playerClass);
            getInventory.setAccessible(true);
            if (getInventory.invoke(chestData, player) != null) {
                throw new IllegalStateException("Legacy player inventory was still readable");
            }

            SharedOwnerState.put(inventories, inventory);
            if (getInventory.invoke(chestData, player) != inventory) {
                throw new IllegalStateException("Shared inventory was not readable across UUIDs");
            }
            if (!LegacyOverlayTruth.hasSharedInventory(inventories)) {
                throw new IllegalStateException("Legacy overlay adapter did not read shared inventory truth");
            }

            Method clearInventory = chestDataClass.getDeclaredMethod("clearInventory", UUID.class);
            clearInventory.setAccessible(true);
            if (!Boolean.TRUE.equals(clearInventory.invoke(chestData, otherPlayer))) {
                throw new IllegalStateException("Shared inventory clear returned false");
            }
            if (SharedOwnerState.contains(inventories) || !inventories.containsKey(legacyOwner)) {
                throw new IllegalStateException("Clear did not remove only the shared inventory");
            }

            Class.forName(chestUtilName);
            System.out.println("SHARED_LOOTR_CHEST_DATA_SMOKE PASS class=" + chestDataName);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException("Shared Lootr ChestData smoke failed", throwable);
        } finally {
            server.halt(false);
        }
    }
}
