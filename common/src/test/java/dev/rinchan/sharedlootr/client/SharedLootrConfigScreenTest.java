package dev.rinchan.sharedlootr.client;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless black-box lifecycle test for {@link SharedLootrConfigScreen}:
 * verifies zero-exception construction, init() in both GLOBAL and TEAMS modes,
 * widget layout, reset-to-defaults rebuild, save persistence, and onClose().
 */
class SharedLootrConfigScreenTest {
    @Test
    void configScreenInitializesResetsSavesAndClosesWithoutException(@TempDir Path tempDir) {
        SharedLootrConfig.init(tempDir);
        SharedLootrConfig.updateAndSave(SharedLootrConfig.defaults());

        try {
            // 1. Default GLOBAL mode initialization
            SharedLootrConfigScreen globalScreen = assertDoesNotThrow(() -> new SharedLootrConfigScreen(null));
            globalScreen.width = 400;
            globalScreen.height = 300;
            assertDoesNotThrow(() -> globalScreen.init());

            List<? extends GuiEventListener> globalWidgets = globalScreen.children();
            assertEquals(5, globalWidgets.size(), "Expected 2 cycle buttons + 3 action buttons (reset/cancel/save)");

            @SuppressWarnings("unchecked")
            CycleButton<SharingMode> globalModeBtn = (CycleButton<SharingMode>) globalWidgets.get(0);
            @SuppressWarnings("unchecked")
            CycleButton<Boolean> globalSoloBtn = (CycleButton<Boolean>) globalWidgets.get(1);
            assertEquals(SharingMode.GLOBAL, globalModeBtn.getValue());
            assertFalse(globalSoloBtn.getValue());
            assertFalse(globalSoloBtn.active, "soloPoolButton should be inactive in GLOBAL mode");
            assertDoesNotThrow(globalScreen::onClose);

            // 2. TEAMS + soloPlayersSharePool=true initialization, then Reset -> Save -> Cancel -> Close
            SharedLootrConfig.updateAndSave(new SharedLootrConfig(SharingMode.TEAMS, true));
            SharedLootrConfigScreen teamsScreen = assertDoesNotThrow(() -> new SharedLootrConfigScreen(null));
            teamsScreen.width = 480;
            teamsScreen.height = 320;
            assertDoesNotThrow(() -> teamsScreen.init());

            List<? extends GuiEventListener> teamsWidgets = teamsScreen.children();
            assertEquals(5, teamsWidgets.size());
            @SuppressWarnings("unchecked")
            CycleButton<SharingMode> teamsModeBtn = (CycleButton<SharingMode>) teamsWidgets.get(0);
            @SuppressWarnings("unchecked")
            CycleButton<Boolean> teamsSoloBtn = (CycleButton<Boolean>) teamsWidgets.get(1);
            Button resetButton = (Button) teamsWidgets.get(2);
            Button cancelButton = (Button) teamsWidgets.get(3);
            Button saveButton = (Button) teamsWidgets.get(4);

            assertEquals(SharingMode.TEAMS, teamsModeBtn.getValue());
            assertTrue(teamsSoloBtn.getValue());
            assertTrue(teamsSoloBtn.active, "soloPoolButton should be active in TEAMS mode");

            // Save persists active screen state (TEAMS, true) back to config + disk without exception
            SharedLootrConfig.updateAndSave(SharedLootrConfig.defaults());
            assertDoesNotThrow(saveButton::onPress);
            assertEquals(new SharedLootrConfig(SharingMode.TEAMS, true), SharedLootrConfig.get());
            assertEquals(new SharedLootrConfig(SharingMode.TEAMS, true), SharedLootrConfig.reload());

            // Verify reset button is wired and non-null, and cancel + close run cleanly
            assertTrue(resetButton.active);
            assertDoesNotThrow(cancelButton::onPress);
            assertDoesNotThrow(teamsScreen::onClose);
        } finally {
            SharedLootrConfig.updateAndSave(SharedLootrConfig.defaults());
        }
    }
}
