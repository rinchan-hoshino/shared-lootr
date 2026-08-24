package dev.rinchan.sharedlootr;

import com.mojang.authlib.GameProfile;
import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.sharedlootr.state.SharedInventoryTruth;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.data.ILootrData;
import noobanidus.mods.lootr.common.api.data.ILootrSection;
import noobanidus.mods.lootr.common.api.filler.ILootFiller;
import noobanidus.mods.lootr.common.data.LootrInventory;
import noobanidus.mods.lootr.common.data.LootrInventoryStore;

import java.lang.reflect.Proxy;
import java.util.UUID;

public final class InventoryStoreSmokeHarness {
    private InventoryStoreSmokeHarness() {
    }

    public static void run(MinecraftServer server) {
        forceMixinTargets();

        UUID firstPlayer = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID secondPlayer = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID dataId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        ILootrData data = proxy(ILootrData.class, (method, args) -> switch (method.getName()) {
            case "getDataId" -> dataId;
            default -> defaultValue(method.getReturnType());
        });
        ILootrContainerInstance instance = proxy(ILootrContainerInstance.class, (method, args) -> switch (method.getName()) {
            case "buildInitialInventory" -> NonNullList.withSize(1, ItemStack.EMPTY);
            case "canPlayerOpen" -> true;
            default -> defaultValue(method.getReturnType());
        });
        ILootFiller filler = proxy(ILootFiller.class, (method, args) -> switch (method.getName()) {
            case "supportsNullPlayers" -> true;
            default -> defaultValue(method.getReturnType());
        });

        ILootrSection section = proxy(ILootrSection.class, (method, args) -> defaultValue(method.getReturnType()));

        LootrInventoryStore store = new LootrInventoryStore(data);
        store.setSection(section);
        check(!SharedInventoryTruth.exists(store), "Empty store was reported as opened");
        ServerPlayer player = new ServerPlayer(
                server,
                server.overworld(),
                new GameProfile(firstPlayer, "shared-lootr-smoke"),
                ClientInformation.createDefault()
        );
        LootrInventory inventory = store.createInventory(instance, player, filler);
        check(inventory != null, "Inventory creation failed");
        check(SharedInventoryTruth.exists(store), "Created shared inventory was not reported as opened");
        check(store.getInventory(firstPlayer) == inventory, "Creator did not resolve the shared inventory");
        check(store.getInventory(secondPlayer) == inventory, "Second player did not resolve the shared inventory");
        check(store.getInventory(SharedOwnerState.OWNER) == inventory, "Shared owner did not resolve the inventory");
        check(store.clearInventories(secondPlayer), "Player-addressed clear did not clear the shared inventory");
        check(store.getInventory(firstPlayer) == null, "Shared inventory remained after clear");
        check(!SharedInventoryTruth.exists(store), "Cleared store remained opened");

        System.out.println("SHARED_LOOTR_INVENTORY_STORE_SMOKE PASS");
    }

    private static void forceMixinTargets() {
        String[] targets = {
                "noobanidus.mods.lootr.common.api.helper.SimpleLootrInstance",
                "noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity",
                "noobanidus.mods.lootr.common.block.entity.LootrBrushableBlockEntity",
                "noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity",
                "noobanidus.mods.lootr.common.block.entity.LootrDecoratedPotBlockEntity",
                "noobanidus.mods.lootr.common.block.entity.LootrShulkerBoxBlockEntity",
                "noobanidus.mods.lootr.common.entity.LootrChestMinecartEntity",
                "noobanidus.mods.lootr.common.entity.LootrItemFrame"
        };
        try {
            for (String target : targets) {
                Class.forName(target);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing Lootr mixin target", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "SharedLootrSmokeProxy(" + type.getSimpleName() + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            return invocation.invoke(method, args);
        });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class || !type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        throw new IllegalArgumentException("Unsupported primitive " + type);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
