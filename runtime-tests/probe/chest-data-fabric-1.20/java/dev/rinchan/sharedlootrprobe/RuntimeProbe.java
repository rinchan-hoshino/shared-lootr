package dev.rinchan.sharedlootrprobe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zestyblaze.lootr.util.ChestUtil;
import net.zestyblaze.lootr.api.LootFiller;
import net.zestyblaze.lootr.block.entities.LootrChestBlockEntity;
import net.zestyblaze.lootr.data.DataStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public final class RuntimeProbe {
    private void require(boolean result, String message) {
        if (!result) throw new IllegalStateException(message);
        System.out.println("SHARED_LOOTR_ASSERT_PASS " + message);
    }
    public void run(MinecraftServer server) {
        server.execute(() -> {
            try {
                var level = server.overworld();
                var position = new BlockPos(0, 80, 0);
                var phase = Path.of("probe-phase1.pass");
                boolean restart = Files.exists(phase);
                if (!restart) {
                    var block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation("lootr", "lootr_chest")).orElseThrow();
                    require(block != net.minecraft.world.level.block.Blocks.AIR, "Lootr block registered");
                    level.setBlockAndUpdate(position, block.defaultBlockState());
                }
                var chest = (LootrChestBlockEntity) level.getBlockEntity(position);
                require(chest != null, "Physical Lootr chest exists in server world");
                if (!restart) chest.setLootTable(new ResourceLocation("minecraft", "chests/simple_dungeon"), 1234L);
                var first = ProbePlayers.create(server, "ProbeOne", restart ? 300 : 100);
                var second = ProbePlayers.create(server, "ProbeTwo", restart ? 400 : 200);
                AtomicInteger generations = new AtomicInteger();
                LootFiller filler = (opener, container, table, seed) -> {
                    generations.incrementAndGet();
                    container.setItem(0, new ItemStack(Items.DIAMOND, 7));
                };
                var a = DataStorage.getInventory(level, chest.getTileId(), position, first, chest, filler);
                var b = DataStorage.getInventory(level, chest.getTileId(), position, second, chest, filler);
                require(a != null && a == b, "Two players resolve the same physical chest inventory");
                if (!restart) {
                    require(generations.get() == 1, "Loot generated once in same tick");
                    require(a.removeItem(0, 1).getCount() == 1 && b.getItem(0).getCount() == 6,
                            "Removal visible to second player without duplication");
                    a.setChanged();
                    ChestUtil.handleLootChest(level.getBlockState(position).getBlock(), level, position, first);
                    require(first.containerMenu.getSlot(0).getItem().getCount() == 6, "Actual chest menu exposes shared inventory");
                    first.closeContainer();
                    require(chest.getOpeners().contains(dev.rinchan.rinlib.state.SharedOwnerState.OWNER), "Opening chest projects shared marker");
                    Files.writeString(phase, "pass\n");
                    System.out.println("SHARED_LOOTR_PHASE1_PASS");
                } else {
                    require(generations.get() == 0 && b.getItem(0).getCount() == 6,
                            "Shared inventory mutation persisted across full server restart");
                    require(DataStorage.getInstance(level, position, chest.getTileId()).clearInventory(second.getUUID()), "Player-scoped clear removes shared inventory");
                    var regenerated = DataStorage.getInventory(level, chest.getTileId(), position, first, chest, filler);
                    require(generations.get() == 1 && regenerated.getItem(0).getCount() == 7,
                            "Inventory regenerated exactly once after clear");
                    Files.writeString(Path.of("probe-phase2.pass"), "pass\n");
                    System.out.println("SHARED_LOOTR_PHASE2_PASS");
                }
            } catch (Throwable error) {
                error.printStackTrace();
                System.out.println("SHARED_LOOTR_RUNTIME_FAIL " + error);
            } finally { server.halt(false); }
        });
    }
}
