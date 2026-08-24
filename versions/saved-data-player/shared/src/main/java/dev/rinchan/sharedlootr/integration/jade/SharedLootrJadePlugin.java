package dev.rinchan.sharedlootr.integration.jade;

import dev.rinchan.sharedlootr.state.SharedSavedDataTruth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class SharedLootrJadePlugin implements IWailaPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath("shared_lootr", "shared_inventory");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(Provider.INSTANCE, BlockEntity.class);
    }

    private enum Provider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof ILootrInfoProvider provider
                    && SharedSavedDataTruth.exists(provider)) {
                data.remove("Loot");
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }

        @Override
        public int getDefaultPriority() {
            return 1100;
        }
    }
}
