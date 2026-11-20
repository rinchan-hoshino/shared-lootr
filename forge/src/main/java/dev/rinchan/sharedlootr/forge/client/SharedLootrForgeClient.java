package dev.rinchan.sharedlootr.forge.client;

import dev.rinchan.sharedlootr.client.SharedLootrConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class SharedLootrForgeClient {
    private SharedLootrForgeClient() {
    }

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new SharedLootrConfigScreen(parent)));
    }
}
