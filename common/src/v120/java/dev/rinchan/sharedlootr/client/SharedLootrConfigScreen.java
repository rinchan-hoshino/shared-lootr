package dev.rinchan.sharedlootr.client;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Native zero-dependency configuration screen for Minecraft 1.20.1.
 */
public final class SharedLootrConfigScreen extends Screen {
    private final Screen parent;
    private SharingMode sharingMode;
    private boolean soloPlayersSharePool;
    private CycleButton<Boolean> soloPoolButton;

    public SharedLootrConfigScreen(Screen parent) {
        super(Component.translatable("screen.shared_lootr.title"));
        this.parent = parent;
        SharedLootrConfig cfg = SharedLootrConfig.get();
        this.sharingMode = cfg.sharingMode();
        this.soloPlayersSharePool = cfg.soloPlayersSharePool();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = Math.min(300, this.width - 40);
        int left = centerX - fieldWidth / 2;
        int y = 52;

        this.addRenderableWidget(CycleButton.<SharingMode>builder(SharedLootrConfigScreen::modeLabel)
                .withValues(SharingMode.GLOBAL, SharingMode.TEAMS)
                .withInitialValue(this.sharingMode)
                .create(left, y, fieldWidth, 20,
                        Component.translatable("screen.shared_lootr.sharing_mode"),
                        (btn, value) -> {
                            this.sharingMode = value;
                            if (this.soloPoolButton != null) {
                                this.soloPoolButton.active = (value == SharingMode.TEAMS);
                            }
                        }));
        y += 30;

        this.soloPoolButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.soloPlayersSharePool)
                .create(left, y, fieldWidth, 20,
                        Component.translatable("screen.shared_lootr.solo_share_pool"),
                        (btn, value) -> this.soloPlayersSharePool = value));
        this.soloPoolButton.active = (this.sharingMode == SharingMode.TEAMS);

        int btnY = Math.min(this.height - 32, y + 66);
        int btnW = (fieldWidth - 12) / 3;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.shared_lootr.reset"),
                        btn -> resetDefaults())
                .bounds(left, btnY, btnW, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.shared_lootr.cancel"),
                        btn -> onClose())
                .bounds(left + btnW + 6, btnY, btnW, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.shared_lootr.save"),
                        btn -> saveAndClose())
                .bounds(left + (btnW + 6) * 2, btnY, fieldWidth - (btnW + 6) * 2, 20)
                .build());
    }

    private static Component modeLabel(SharingMode mode) {
        return Component.translatable(mode == SharingMode.TEAMS
                ? "screen.shared_lootr.mode.teams"
                : "screen.shared_lootr.mode.global");
    }

    private void resetDefaults() {
        SharedLootrConfig defaults = SharedLootrConfig.defaults();
        this.sharingMode = defaults.sharingMode();
        this.soloPlayersSharePool = defaults.soloPlayersSharePool();
        this.rebuildWidgets();
    }

    private void saveAndClose() {
        SharedLootrConfig.updateAndSave(new SharedLootrConfig(this.sharingMode, this.soloPlayersSharePool));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        Component desc = Component.translatable(this.sharingMode == SharingMode.TEAMS
                ? "screen.shared_lootr.mode_desc.teams"
                : "screen.shared_lootr.mode_desc.global");
        graphics.drawCenteredString(this.font, desc, this.width / 2, 114, 0xB8C4D0);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
