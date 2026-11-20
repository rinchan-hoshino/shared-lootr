package dev.rinchan.sharedlootr.forge;

import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.client.SharedLootrConfigScreen;
import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(SharedLootr.MOD_ID)
public final class SharedLootrForge {
    public SharedLootrForge() {
        SharedLootr.init(FMLPaths.CONFIGDIR.get());
        PlayerPoolResolver.setServerPlayerLookup(uuid -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server != null ? server.getPlayerList().getPlayer(uuid) : null;
        });
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> SharedLootrForge::registerConfigScreen);
    }

    private static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new SharedLootrConfigScreen(parent)));
    }
}
