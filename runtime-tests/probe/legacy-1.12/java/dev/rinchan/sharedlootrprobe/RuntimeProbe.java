package dev.rinchan.sharedlootrprobe;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import noobanidus.mods.lootr.api.LootFiller;
import noobanidus.mods.lootr.block.tile.LootrChestTileEntity;
import noobanidus.mods.lootr.data.DataStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class RuntimeProbe {
    private EntityPlayerMP player(MinecraftServer server, String name, long id) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(server.getWorld(0), new GameProfile(new UUID(0L, id), name));
    }
    private void require(boolean result, String message) {
        if (!result) throw new IllegalStateException(message);
        System.out.println("SHARED_LOOTR_ASSERT_PASS " + message);
    }
    public void run(MinecraftServer server) {
        server.addScheduledTask(() -> {
            try {
                net.minecraft.world.WorldServer level = server.getWorld(0);
                BlockPos position = new BlockPos(0, 80, 0);
                Path phase = java.nio.file.Paths.get("probe-phase1.pass");
                boolean restart = Files.exists(phase);
                if (!restart) {
                    net.minecraft.block.Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("lootr", "lootr_chest"));
                    require(block != net.minecraft.init.Blocks.AIR, "Lootr block registered");
                    level.setBlockState(position, block.getDefaultState());
                }
                LootrChestTileEntity chest = (LootrChestTileEntity) level.getTileEntity(position);
                require(chest != null, "Physical Lootr chest exists in server world");
                EntityPlayerMP first = player(server, "ProbeOne", restart ? 300 : 100);
                EntityPlayerMP second = player(server, "ProbeTwo", restart ? 400 : 200);
                AtomicInteger generations = new AtomicInteger();
                LootFiller filler = (opener, container, table, seed) -> {
                    generations.incrementAndGet();
                    container.setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 7));
                };
                net.minecraft.inventory.IInventory a = DataStorage.getInventory(level, chest.getTileId(), position, first, chest, filler);
                net.minecraft.inventory.IInventory b = DataStorage.getInventory(level, chest.getTileId(), position, second, chest, filler);
                require(a != null && a == b, "Two players resolve the same physical chest inventory");
                if (!restart) {
                    require(generations.get() == 1, "Loot generated once in same tick");
                    require(a.decrStackSize(0, 1).getCount() == 1 && b.getStackInSlot(0).getCount() == 6,
                            "Removal visible to second player without duplication");
                    a.markDirty();
                    chest.openInventory(first);
                    chest.closeInventory(first);
                    require(chest.getOpeners().contains(dev.rinchan.rinlib.state.SharedOwnerState.OWNER),
                            "Closing chest projects shared opened marker");
                    Files.write(phase, "pass\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    System.out.println("SHARED_LOOTR_PHASE1_PASS");
                } else {
                    require(generations.get() == 0 && b.getStackInSlot(0).getCount() == 6,
                            "Shared inventory mutation persisted across full server restart");
                    require(DataStorage.getInstance(level, chest.getTileId(), position).clearInventory(second.getUniqueID()), "Player-scoped clear removes shared inventory");
                    net.minecraft.inventory.IInventory regenerated = DataStorage.getInventory(level, chest.getTileId(), position, first, chest, filler);
                    require(generations.get() == 1 && regenerated.getStackInSlot(0).getCount() == 7,
                            "Inventory regenerated exactly once after clear");
                    Files.write(java.nio.file.Paths.get("probe-phase2.pass"), "pass\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    System.out.println("SHARED_LOOTR_PHASE2_PASS");
                }
            } catch (Throwable error) {
                error.printStackTrace();
                System.out.println("SHARED_LOOTR_RUNTIME_FAIL " + error);
            } finally { server.initiateShutdown(); }
        });
    }
}
