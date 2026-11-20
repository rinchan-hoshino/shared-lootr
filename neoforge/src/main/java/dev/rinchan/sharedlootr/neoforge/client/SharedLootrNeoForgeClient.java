package dev.rinchan.sharedlootr.neoforge.client;

import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.client.SharedLootrConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SharedLootr.MOD_ID, dist = Dist.CLIENT)
public final class SharedLootrNeoForgeClient {
    public SharedLootrNeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, parent) -> new SharedLootrConfigScreen(parent));
    }
}
