package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
import java.util.function.Supplier;

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
            dimensionField.set(chestData, server.overworld().dimension());

            UUID playerId = UUID.randomUUID();
            ServerPlayer player = new ServerPlayer(
                    server,
                    server.overworld(),
                    new GameProfile(playerId, "SharedLootrSmoke")
            );

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
            Supplier<Component> display = () -> Component.literal("Shared Lootr smoke");
            Supplier<ResourceLocation> table = () -> new ResourceLocation("minecraft", "empty");
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

            Class<?> infoProviderClass = Class.forName(infoProviderName);
            Set<UUID> openers = new HashSet<>();
            Object provider = Proxy.newProxyInstance(
                    infoProviderClass.getClassLoader(),
                    new Class<?>[]{infoProviderClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getOpeners")) {
                            return openers;
                        }
                        return null;
                    }
            );
            Class<?> chestUtilClass = Class.forName(chestUtilName);
            Method addOpener = chestUtilClass.getDeclaredMethod("addOpener", infoProviderClass, Player.class);
            addOpener.setAccessible(true);
            addOpener.invoke(null, provider, player);
            if (!openers.contains(SharedOwnerState.OWNER) || openers.contains(playerId)) {
                throw new IllegalStateException("Opened projection was not stored under the shared owner");
            }

            System.out.println("SHARED_LOOTR_CHEST_DATA_SMOKE PASS class=" + chestDataName);
            server.halt(false);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Shared Lootr ChestData smoke failed", exception);
        }
    }
}
