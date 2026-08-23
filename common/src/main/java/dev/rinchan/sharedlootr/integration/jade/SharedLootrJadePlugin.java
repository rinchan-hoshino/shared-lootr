package dev.rinchan.sharedlootr.integration.jade;

import dev.rinchan.sharedlootr.SharedLootr;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class SharedLootrJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(GeneratedLootStateProvider.INSTANCE, BlockEntity.class);
    }

    private enum GeneratedLootStateProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
                SharedLootr.MOD_ID,
                "generated_loot_state"
        );

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof ILootrInfoProvider provider
                    && provider.hasBeenOpened()) {
                tag.remove("Loot");
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public int getDefaultPriority() {
            return 1100;
        }
    }
}
