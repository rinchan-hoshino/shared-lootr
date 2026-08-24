package dev.rinchan.sharedlootr.integration.jade;

import dev.rinchan.sharedlootr.state.SharedInventoryTruth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class SharedLootrJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(Provider.INSTANCE, BlockEntity.class);
    }

    private enum Provider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        private static final Identifier ID = Identifier.fromNamespaceAndPath("shared_lootr", "shared_inventory");

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(LootrAPI.wrapBlockEntity(accessor.getBlockEntity()) instanceof ILootrBlockEntity instance)) {
                return;
            }
            if (SharedInventoryTruth.exists(instance)) {
                data.remove("Loot");
            }
        }

        @Override
        public Identifier getUid() {
            return ID;
        }
    }
}
