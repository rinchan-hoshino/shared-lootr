package dev.rinchan.sharedlootr.e2e;

import com.mojang.authlib.GameProfile;
import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.registry.LootrRegistry;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import noobanidus.mods.lootr.neoforge.config.ConfigManager;

import java.net.URL;
import java.util.UUID;

/**
 * Packaged-JAR real-runtime black-box GameTest suite executed inside NeoForge 1.21.1 {@code gameTestServer}
 * with the real packaged {@code shared_lootr-neoforge-2.0.0+1.21.1.jar} and real upstream {@code Lootr} JAR.
 */
@GameTestHolder(SharedLootrE2eMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SharedLootrPackagedJarGameTests {
    private static final ResourceKey<LootTable> DIAMONDS_LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(SharedLootrE2eMod.MOD_ID, "diamonds"));

    private static final UUID ALICE_UUID = UUID.fromString("aaaaaaaa-1111-4111-8111-111111111111");
    private static final UUID BOB_UUID = UUID.fromString("bbbbbbbb-2222-4222-8222-222222222222");

    private SharedLootrPackagedJarGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void packagedJarAndMixinLoaded(GameTestHelper helper) {
        URL codeSource = SharedLootr.class.getProtectionDomain().getCodeSource().getLocation();
        String location = codeSource != null ? codeSource.toString() : "";
        String expectedJar = System.getProperty("sharedlootr.e2e.expectedJar", ".jar");
        if (!location.endsWith(expectedJar) && !location.contains(expectedJar)) {
            throw new GameTestAssertException(
                    "SharedLootr must be loaded from packaged JAR (" + expectedJar + "), but loaded from: " + location);
        }

        if (!LootrAPI.isTeamLoot()) {
            throw new GameTestAssertException("LootrTeamConfigMixin failed: LootrAPI.isTeamLoot() returned false");
        }
        ResourceLocation expectedResolver = ResourceLocation.fromNamespaceAndPath(SharedLootr.MOD_ID, "shared");
        ResourceLocation pinned = LootrAPI.getPinnedTeamResolver();
        if (!expectedResolver.equals(pinned)) {
            throw new GameTestAssertException(
                    "LootrTeamConfigMixin failed: expected pinned resolver " + expectedResolver + ", got " + pinned);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void globalModeSharedLootJourney(GameTestHelper helper) {
        SharedLootrConfig previous = SharedLootrConfig.get();
        try {
            SharedLootrConfig.updateAndSave(new SharedLootrConfig(SharingMode.GLOBAL, false));

            ServerLevel level = helper.getLevel();
            BlockPos absPos = placeLootrChestWithDiamonds(helper, new BlockPos(2, 1, 2));

            ServerPlayer alice = createRealServerPlayer(level, ALICE_UUID, "Alice", absPos);
            ServerPlayer bob = createRealServerPlayer(level, BOB_UUID, "Bob", absPos);

            // Alice opens the Lootr chest through real block interaction
            openChest(level, absPos, alice);
            int aliceInitial = countChestDiamonds(alice.containerMenu);
            if (aliceInitial != 4) {
                throw new GameTestAssertException("Expected Alice to see 4 diamonds in GLOBAL chest, saw " + aliceInitial);
            }

            // Alice takes 2 diamonds and closes
            int taken = takeChestDiamonds(alice.containerMenu, 2);
            if (taken != 2) {
                throw new GameTestAssertException("Expected Alice to take 2 diamonds, took " + taken);
            }
            alice.closeContainer();

            // Bob (different UUID) opens the exact same chest -> must see only 2 diamonds remaining
            openChest(level, absPos, bob);
            int bobRemaining = countChestDiamonds(bob.containerMenu);
            bob.closeContainer();
            if (bobRemaining != 2) {
                throw new GameTestAssertException(
                        "GLOBAL mode sharing broken: expected Bob to see 2 remaining diamonds, saw " + bobRemaining);
            }

            helper.succeed();
        } finally {
            SharedLootrConfig.updateAndSave(previous);
        }
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void soloModeIsolatedLootJourney(GameTestHelper helper) {
        SharedLootrConfig previous = SharedLootrConfig.get();
        try {
            // TEAMS mode with soloPlayersSharePool = false => solo players keep independent per-player pools
            SharedLootrConfig.updateAndSave(new SharedLootrConfig(SharingMode.TEAMS, false));

            ServerLevel level = helper.getLevel();
            BlockPos absPos = placeLootrChestWithDiamonds(helper, new BlockPos(2, 1, 2));

            ServerPlayer alice = createRealServerPlayer(level, ALICE_UUID, "AliceSolo", absPos);
            ServerPlayer bob = createRealServerPlayer(level, BOB_UUID, "BobSolo", absPos);

            // Alice opens and empties all 4 diamonds
            openChest(level, absPos, alice);
            int aliceInitial = countChestDiamonds(alice.containerMenu);
            if (aliceInitial != 4) {
                throw new GameTestAssertException("Expected Alice to see 4 diamonds in SOLO chest, saw " + aliceInitial);
            }
            takeChestDiamonds(alice.containerMenu, 4);
            alice.closeContainer();

            // Bob (different UUID, also solo) opens the same chest -> must still have all 4 diamonds
            openChest(level, absPos, bob);
            int bobDiamonds = countChestDiamonds(bob.containerMenu);
            bob.closeContainer();
            if (bobDiamonds != 4) {
                throw new GameTestAssertException(
                        "SOLO mode isolation broken: expected Bob to see 4 diamonds, saw " + bobDiamonds);
            }

            helper.succeed();
        } finally {
            SharedLootrConfig.updateAndSave(previous);
        }
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void nonSneakBreakBlockedAndSneakBreakAllowed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos absPos = placeLootrChestWithDiamonds(helper, new BlockPos(2, 1, 2));
        ServerPlayer alice = createRealServerPlayer(level, ALICE_UUID, "AliceBreaker", absPos);

        // Non-sneaking break must be intercepted and canceled by Lootr's HandleBreak
        alice.setShiftKeyDown(false);
        boolean brokenWithoutSneak = alice.gameMode.destroyBlock(absPos);
        if (brokenWithoutSneak) {
            throw new GameTestAssertException("Non-sneak break of Lootr chest should have been blocked!");
        }
        if (!level.getBlockState(absPos).is(LootrRegistry.getChestBlock())) {
            throw new GameTestAssertException("Lootr chest block disappeared after blocked non-sneak break!");
        }

        // Sneaking break must be allowed
        alice.setShiftKeyDown(true);
        boolean brokenWithSneak = alice.gameMode.destroyBlock(absPos);
        if (!brokenWithSneak) {
            throw new GameTestAssertException("Sneak break of Lootr chest should have been allowed!");
        }
        if (!level.getBlockState(absPos).isAir()) {
            throw new GameTestAssertException(
                    "Expected air after sneak break, got " + level.getBlockState(absPos));
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void refreshHookRegeneratesSharedPool(GameTestHelper helper) {
        SharedLootrConfig previousCfg = SharedLootrConfig.get();
        boolean prevRefreshAll = ConfigManager.REFRESH_ALL.get();
        int prevRefreshValue = ConfigManager.REFRESH_VALUE.get();
        boolean prevDecayAll = ConfigManager.DECAY_ALL.get();

        SharedLootrConfig.updateAndSave(new SharedLootrConfig(SharingMode.GLOBAL, false));
        ConfigManager.DECAY_ALL.set(false);
        ConfigManager.REFRESH_ALL.set(true);
        ConfigManager.REFRESH_VALUE.set(1);

        ServerLevel level = helper.getLevel();
        BlockPos absPos = placeLootrChestWithDiamonds(helper, new BlockPos(2, 1, 2));
        ServerPlayer alice = createRealServerPlayer(level, ALICE_UUID, "AliceRefresh", absPos);
        ServerPlayer bob = createRealServerPlayer(level, BOB_UUID, "BobRefresh", absPos);

        // Alice opens and takes all 4 diamonds, which arms Lootr's refresh timer
        openChest(level, absPos, alice);
        int taken = takeChestDiamonds(alice.containerMenu, 4);
        alice.closeContainer();
        if (taken != 4) {
            restoreConfigs(previousCfg, prevRefreshAll, prevRefreshValue, prevDecayAll, ConfigManager.DECAY_VALUE.get());
            throw new GameTestAssertException("Expected Alice to take 4 diamonds before refresh, took " + taken);
        }

        // Wait 5 ticks (> REFRESH_VALUE=1), then Bob opens -> refresh hook regenerates 4 diamonds
        helper.runAfterDelay(5, () -> {
            try {
                openChest(level, absPos, bob);
                int refreshedDiamonds = countChestDiamonds(bob.containerMenu);
                bob.closeContainer();
                if (refreshedDiamonds != 4) {
                    throw new GameTestAssertException(
                            "Lootr refresh hook failed: expected 4 regenerated diamonds, saw " + refreshedDiamonds);
                }
                helper.succeed();
            } finally {
                restoreConfigs(previousCfg, prevRefreshAll, prevRefreshValue, prevDecayAll, ConfigManager.DECAY_VALUE.get());
            }
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void decayHookDestroysExpiredChest(GameTestHelper helper) {
        SharedLootrConfig previousCfg = SharedLootrConfig.get();
        boolean prevRefreshAll = ConfigManager.REFRESH_ALL.get();
        int prevRefreshValue = ConfigManager.REFRESH_VALUE.get();
        boolean prevDecayAll = ConfigManager.DECAY_ALL.get();
        int prevDecayValue = ConfigManager.DECAY_VALUE.get();

        SharedLootrConfig.updateAndSave(new SharedLootrConfig(SharingMode.GLOBAL, false));
        ConfigManager.REFRESH_ALL.set(false);
        ConfigManager.DECAY_ALL.set(true);
        ConfigManager.DECAY_VALUE.set(1);

        ServerLevel level = helper.getLevel();
        BlockPos absPos = placeLootrChestWithDiamonds(helper, new BlockPos(2, 1, 2));
        ServerPlayer alice = createRealServerPlayer(level, ALICE_UUID, "AliceDecay", absPos);
        ServerPlayer bob = createRealServerPlayer(level, BOB_UUID, "BobDecay", absPos);

        // Alice opens chest, which arms Lootr's decay timer
        openChest(level, absPos, alice);
        alice.closeContainer();

        // Wait 5 ticks (> DECAY_VALUE=1), then Bob interacts -> decay hook triggers performDecay()
        helper.runAfterDelay(5, () -> {
            try {
                BlockState state = level.getBlockState(absPos);
                if (state.is(LootrRegistry.getChestBlock())) {
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
                    state.useWithoutItem(level, bob, hit);
                }
                if (level.getBlockState(absPos).is(LootrRegistry.getChestBlock())) {
                    throw new GameTestAssertException(
                            "Lootr decay hook failed: chest at " + absPos + " was not decayed after expiry!");
                }
                helper.succeed();
            } finally {
                restoreConfigs(previousCfg, prevRefreshAll, prevRefreshValue, prevDecayAll, prevDecayValue);
            }
        });
    }

    private static void restoreConfigs(
            SharedLootrConfig prevCfg,
            boolean prevRefreshAll,
            int prevRefreshValue,
            boolean prevDecayAll,
            int prevDecayValue) {
        SharedLootrConfig.updateAndSave(prevCfg);
        ConfigManager.REFRESH_ALL.set(prevRefreshAll);
        ConfigManager.REFRESH_VALUE.set(prevRefreshValue);
        ConfigManager.DECAY_ALL.set(prevDecayAll);
        ConfigManager.DECAY_VALUE.set(prevDecayValue);
    }

    private static BlockPos placeLootrChestWithDiamonds(GameTestHelper helper, BlockPos relPos) {
        ServerLevel level = helper.getLevel();
        BlockPos absPos = helper.absolutePos(relPos);
        BlockState chestState = LootrRegistry.getChestBlock().defaultBlockState();
        level.setBlock(absPos, chestState, Block.UPDATE_ALL);
        BlockEntity be = level.getBlockEntity(absPos);
        if (!(be instanceof LootrChestBlockEntity chest)) {
            throw new GameTestAssertException("Expected LootrChestBlockEntity at " + absPos + ", got " + be);
        }
        chest.setLootTable(DIAMONDS_LOOT_TABLE, 12345L);
        chest.setChanged();
        return absPos;
    }

    private static ServerPlayer createRealServerPlayer(ServerLevel level, UUID uuid, String name, BlockPos nearPos) {
        GameProfile profile = new GameProfile(uuid, name);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
                // Embedded mock connection: no remote client socket to serialize custom payloads to
            }

            @Override
            public void send(Packet<?> packet, PacketSendListener listener) {
                // Embedded mock connection: no remote client socket to serialize custom payloads to
            }
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(nearPos.getX() + 0.5, nearPos.getY() + 1.0, nearPos.getZ() + 0.5);
        return player;
    }

    private static void openChest(ServerLevel level, BlockPos absPos, ServerPlayer player) {
        BlockState state = level.getBlockState(absPos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
        InteractionResult result = state.useWithoutItem(level, player, hit);
        if (!result.consumesAction()) {
            throw new GameTestAssertException("Failed to open Lootr chest for " + player.getName().getString() + ": " + result);
        }
        if (player.containerMenu == player.inventoryMenu) {
            throw new GameTestAssertException("Lootr chest did not open a container menu for " + player.getName().getString());
        }
    }

    private static int countChestDiamonds(AbstractContainerMenu menu) {
        int total = 0;
        for (int i = 0; i < 27; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.is(Items.DIAMOND)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int takeChestDiamonds(AbstractContainerMenu menu, int amountToTake) {
        int taken = 0;
        for (int i = 0; i < 27 && taken < amountToTake; i++) {
            Slot slot = menu.getSlot(i);
            ItemStack stack = slot.getItem();
            if (stack.is(Items.DIAMOND)) {
                int take = Math.min(amountToTake - taken, stack.getCount());
                stack.shrink(take);
                slot.setChanged();
                taken += take;
            }
        }
        return taken;
    }
}
