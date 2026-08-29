package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.state.SharedSavedDataTruth;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import noobanidus.mods.lootr.common.api.data.ILootrInfo;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.LootFiller;
import noobanidus.mods.lootr.common.data.LootrInventory;
import noobanidus.mods.lootr.common.data.LootrSavedData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

public final class SavedDataSmokeHarness {
    private SavedDataSmokeHarness() {
    }

    public static void run(MinecraftServer server) {
        try {
            forceMixinTargets();
            ILootrInfo info = proxy(ILootrInfo.class);
            Constructor<LootrSavedData> constructor = LootrSavedData.class.getDeclaredConstructor(ILootrInfo.class);
            constructor.setAccessible(true);
            LootrSavedData data = constructor.newInstance(info);
            ILootrInfoProvider provider = (ILootrInfoProvider) Proxy.newProxyInstance(
                    SavedDataSmokeHarness.class.getClassLoader(),
                    new Class<?>[]{ILootrInfoProvider.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "buildInitialInventory" -> NonNullList.withSize(1, ItemStack.EMPTY);
                        case "canPlayerOpen" -> true;
                        default -> defaultValue(method.getReturnType());
                    }
            );
            LootFiller filler = proxy(LootFiller.class);
            UUID firstPlayer = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID secondPlayer = UUID.fromString("22222222-2222-2222-2222-222222222222");
            Field inventoriesField = LootrSavedData.class.getDeclaredField("inventories");
            inventoriesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, LootrInventory> inventories = (Map<UUID, LootrInventory>) inventoriesField.get(data);
            LootrInventory legacy = new LootrInventory(data, NonNullList.withSize(1, ItemStack.EMPTY));
            inventories.put(firstPlayer, legacy);

            if (data.getInventory(firstPlayer) != null) {
                throw new IllegalStateException("Legacy player inventory was still readable");
            }
            if (SharedSavedDataTruth.exists(data)) {
                throw new IllegalStateException("Fresh saved data reported a shared inventory");
            }
            ServerPlayer player = new ServerPlayer(
                    server,
                    server.overworld(),
                    new GameProfile(firstPlayer, "shared-lootr-first"),
                    ClientInformation.createDefault()
            );
            LootrInventory created = data.createInventory(provider, player, filler);
            if (created == null || data.getInventory(firstPlayer) != created || data.getInventory(secondPlayer) != created) {
                throw new IllegalStateException("Two player UUIDs did not resolve the same shared inventory");
            }
            if (!SharedSavedDataTruth.exists(data)) {
                throw new IllegalStateException("Shared saved-data truth did not observe the created inventory");
            }
            if (!data.clearInventories(firstPlayer) || SharedSavedDataTruth.exists(data)) {
                throw new IllegalStateException("Player-scoped clear did not clear shared inventory truth");
            }
            System.out.println("SHARED_LOOTR_SAVED_DATA_SMOKE PASS");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Saved-data smoke harness failed", exception);
        }
    }

    private static void forceMixinTargets() throws ClassNotFoundException {
        Class.forName("noobanidus.mods.lootr.common.data.LootrSavedData");
        Class.forName("noobanidus.mods.lootr.common.impl.DefaultLootrAPIImpl");
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                SavedDataSmokeHarness.class.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> defaultValue(method.getReturnType())
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        throw new IllegalArgumentException("Unsupported primitive: " + type);
    }
}
