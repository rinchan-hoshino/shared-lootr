package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public final class ChestDataSmokeHarness {
    private ChestDataSmokeHarness() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void run(
            MinecraftServer server,
            String chestDataName,
            String infoProviderName,
            String chestUtilName
    ) {
        try {
            Class<?> chestDataClass = Class.forName(chestDataName);
            Constructor<?> constructor = chestDataClass.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            Object chestData = constructor.newInstance("shared_lootr_smoke");

            Field inventoriesField = chestDataClass.getDeclaredField("inventories");
            inventoriesField.setAccessible(true);
            Map<UUID, Object> inventories = (Map<UUID, Object>) inventoriesField.get(chestData);

            Field dimensionField = chestDataClass.getDeclaredField("dimension");
            dimensionField.setAccessible(true);
            Object overworld = server.overworld();
            Method dimension = overworld.getClass().getMethod("dimension");
            dimensionField.set(chestData, dimension.invoke(overworld));

            UUID playerId = UUID.randomUUID();
            ServerPlayer player = null;
            for (Constructor<?> candidate : ServerPlayer.class.getDeclaredConstructors()) {
                Class<?>[] parameters = candidate.getParameterTypes();
                if (parameters.length >= 3
                        && MinecraftServer.class.isAssignableFrom(parameters[0])
                        && parameters[2] == GameProfile.class) {
                    Object[] arguments = new Object[parameters.length];
                    arguments[0] = server;
                    arguments[1] = server.overworld();
                    arguments[2] = new GameProfile(playerId, "SharedLootrSmoke");
                    candidate.setAccessible(true);
                    player = (ServerPlayer) candidate.newInstance(arguments);
                    break;
                }
            }
            if (player == null) {
                throw new IllegalStateException("Compatible ServerPlayer constructor was not found");
            }

            Method getInventory = chestDataClass.getMethod("getInventory", ServerPlayer.class);
            inventories.put(playerId, new Object());
            if (getInventory.invoke(chestData, player) != null) {
                throw new IllegalStateException("Legacy player inventory was still readable");
            }

            Method createInventory = null;
            for (Method method : chestDataClass.getMethods()) {
                if (method.getName().equals("createInventory") && method.getParameterCount() == 6) {
                    createInventory = method;
                    break;
                }
            }
            if (createInventory == null) {
                throw new IllegalStateException("Six-argument createInventory overload was not found");
            }

            Class<?> lootFillerClass = createInventory.getParameterTypes()[1];
            Object noOpFiller = Proxy.newProxyInstance(
                    lootFillerClass.getClassLoader(),
                    new Class<?>[]{lootFillerClass},
                    (proxy, method, args) -> null
            );
            java.util.function.Supplier<Object> display = () -> null;
            java.util.function.Supplier<Object> table = () -> null;
            Object created = createInventory.invoke(
                    chestData,
                    player,
                    noOpFiller,
                    (IntSupplier) () -> 1,
                    display,
                    table,
                    (LongSupplier) () -> 0L
            );

            if (created == null || inventories.get(SharedOwnerState.OWNER) != created) {
                throw new IllegalStateException("Created inventory was not stored under the shared owner");
            }
            if (inventories.get(playerId) == created) {
                throw new IllegalStateException("Created inventory remained player-owned");
            }
            if (!LegacyOverlayTruth.hasSharedInventory(inventories)) {
                throw new IllegalStateException("Legacy overlay adapter did not read shared inventory truth");
            }
            if (getInventory.invoke(chestData, player) != created) {
                throw new IllegalStateException("Shared inventory lookup did not return the created inventory");
            }

            Method clearInventory = chestDataClass.getMethod("clearInventory", UUID.class);
            if (!Boolean.TRUE.equals(clearInventory.invoke(chestData, playerId))) {
                throw new IllegalStateException("Shared inventory clear did not report success");
            }
            if (inventories.containsKey(SharedOwnerState.OWNER) || getInventory.invoke(chestData, player) != null) {
                throw new IllegalStateException("Shared inventory clear did not remove shared truth");
            }
            if (!inventories.containsKey(playerId)) {
                throw new IllegalStateException("Shared inventory clear removed legacy player data");
            }

            Class<?> chestUtilClass = Class.forName(chestUtilName);
            try {
                Class<?> infoProviderClass = Class.forName(infoProviderName);
                Set<UUID> openers = new HashSet<>();
                Object provider = Proxy.newProxyInstance(
                        infoProviderClass.getClassLoader(),
                        new Class<?>[]{infoProviderClass},
                        (proxy, method, args) -> method.getName().equals("getOpeners") ? openers : null
                );
                Method addOpener = null;
                for (Method method : chestUtilClass.getDeclaredMethods()) {
                    if (method.getName().equals("addOpener") && method.getParameterCount() == 2) {
                        addOpener = method;
                        break;
                    }
                }
                if (addOpener != null) {
                    addOpener.setAccessible(true);
                    addOpener.invoke(null, provider, player);
                    if (!openers.contains(SharedOwnerState.OWNER) || openers.contains(playerId)) {
                        throw new IllegalStateException("Opened projection was not stored under the shared owner");
                    }
                }
            } catch (ClassNotFoundException ignoredOlderProjectionApi) {
                // Loading ChestUtil above proves the matching old projection mixin target.
            }

            System.out.println("SHARED_LOOTR_CHEST_DATA_SMOKE PASS class=" + chestDataName);
            server.halt(false);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Shared Lootr ChestData smoke failed", exception);
        }
    }
}
