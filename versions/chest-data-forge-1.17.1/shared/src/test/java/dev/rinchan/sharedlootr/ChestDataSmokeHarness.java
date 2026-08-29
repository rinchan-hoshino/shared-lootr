package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public final class ChestDataSmokeHarness {
    private ChestDataSmokeHarness() {
    }

    public static void run(MinecraftServer server, String chestDataName, String ignoredInfoProviderName, String chestUtilName) {
        try {
            Class<?> chestDataClass = Class.forName(chestDataName);
            Constructor<?> dataConstructor = chestDataClass.getDeclaredConstructor(String.class);
            dataConstructor.setAccessible(true);
            Object chestData = dataConstructor.newInstance("shared_lootr_smoke");

            Field inventoriesField = chestDataClass.getDeclaredField("inventories");
            inventoriesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Object> inventories = (Map<UUID, Object>) inventoriesField.get(chestData);

            Class<?> inventoryClass = Class.forName("noobanidus.mods.lootr.data.SpecialChestInventory");
            Constructor<?> inventoryConstructor = inventoryClass.getDeclaredConstructors()[0];
            inventoryConstructor.setAccessible(true);
            Object[] inventoryArguments = new Object[inventoryConstructor.getParameterCount()];
            inventoryArguments[0] = chestData;
            Class<?> nonNullList = Class.forName("net.minecraft.core.NonNullList");
            inventoryArguments[1] = nonNullList.getMethod("create").invoke(null);
            Object inventory = inventoryConstructor.newInstance(inventoryArguments);

            UUID legacyOwner = UUID.randomUUID();
            UUID otherPlayer = UUID.randomUUID();
            inventories.put(legacyOwner, inventory);

            ServerPlayer player = null;
            for (Constructor<?> constructor : ServerPlayer.class.getDeclaredConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if (parameters.length >= 3 && parameters[0] == MinecraftServer.class && parameters[2] == GameProfile.class) {
                    Object[] arguments = new Object[parameters.length];
                    arguments[0] = server;
                    arguments[1] = server.overworld();
                    arguments[2] = new GameProfile(legacyOwner, "SharedLootrSmoke");
                    constructor.setAccessible(true);
                    player = (ServerPlayer) constructor.newInstance(arguments);
                    break;
                }
            }
            if (player == null) {
                throw new IllegalStateException("ServerPlayer constructor was not found");
            }

            Method getInventory = null;
            for (Method method : chestDataClass.getDeclaredMethods()) {
                if (method.getName().equals("getInventory")) {
                    getInventory = method;
                    break;
                }
            }
            if (getInventory == null) {
                throw new IllegalStateException("NewChestData.getInventory was not found");
            }
            getInventory.setAccessible(true);
            Object[] getArguments = new Object[getInventory.getParameterCount()];
            getArguments[0] = player;
            if (getInventory.invoke(chestData, getArguments) != null) {
                throw new IllegalStateException("Legacy player inventory was still readable");
            }

            SharedOwnerState.put(inventories, inventory);
            if (getInventory.invoke(chestData, getArguments) != inventory) {
                throw new IllegalStateException("Shared inventory was not returned");
            }
            if (!LegacyOverlayTruth.hasSharedInventory(inventories)) {
                throw new IllegalStateException("Overlay adapter did not read shared inventory truth");
            }

            Method clearInventory = chestDataClass.getDeclaredMethod("clearInventory", UUID.class);
            if (!Boolean.TRUE.equals(clearInventory.invoke(chestData, otherPlayer))) {
                throw new IllegalStateException("Shared inventory clear was not redirected");
            }
            if (SharedOwnerState.contains(inventories) || inventories.get(legacyOwner) != inventory) {
                throw new IllegalStateException("Clear did not preserve ignored legacy data");
            }

            Class.forName(chestUtilName);
            System.out.println("SHARED_LOOTR_CHEST_DATA_SMOKE PASS class=" + chestDataName);
        } catch (Throwable failure) {
            failure.printStackTrace();
            throw new RuntimeException("Shared Lootr ChestData smoke failed", failure);
        } finally {
            server.halt(false);
        }
    }
}
