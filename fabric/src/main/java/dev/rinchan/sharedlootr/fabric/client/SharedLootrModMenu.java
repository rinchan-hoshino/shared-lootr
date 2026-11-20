package dev.rinchan.sharedlootr.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rinchan.sharedlootr.client.SharedLootrConfigScreen;

public final class SharedLootrModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SharedLootrConfigScreen::new;
    }
}
