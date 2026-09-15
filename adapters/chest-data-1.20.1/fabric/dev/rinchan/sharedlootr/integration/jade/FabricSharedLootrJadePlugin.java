package dev.rinchan.sharedlootr.integration.jade;

import dev.rinchan.rinlib.state.SharedOwnerState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zestyblaze.lootr.api.blockentity.ILootBlockEntity;
import net.zestyblaze.lootr.data.ChestData;
import net.zestyblaze.lootr.data.DataStorage;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class FabricSharedLootrJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(Provider.INSTANCE, BlockEntity.class);
    }

    private enum Provider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        private static final ResourceLocation ID = new ResourceLocation("shared_lootr", "shared_inventory");

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BlockEntity blockEntity = accessor.getBlockEntity();
            if (!(blockEntity instanceof ILootBlockEntity provider)
                    || !(accessor.getLevel() instanceof ServerLevel level)) {
                return;
            }
            ChestData chestData = DataStorage.getContainerData(
                    level,
                    provider.getPosition(),
                    provider.getTileId()
            );
            if (chestData.getInventory(SharedOwnerState.OWNER) != null) {
                data.remove("Loot");
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ID;
        }
    }
}
