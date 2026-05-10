package com.mef.enoughfishing.gui;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.gui.elements.MEFSlider;
import com.mef.enoughfishing.gui.elements.ToggleButton;
import com.mef.enoughfishing.utils.ColorUtils;
import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main configuration screen, opened via {@code /mef}.
 *
 * <p>Layout (fixed pixel positions set in {@link #initGui()}):</p>
 * <pre>
 * ┌───────────────────────────────────────────┐  panelY
 * │  ≡ Enough Fishing  v1.0.0                 │  title bar 24px
 * ├───── ⏱ Timer ────────────────────────────┤  sectionY[0]
 * │  [✔ Timer]    [✔ Show MS]                 │
 * │  R ─────────────────── 255                │
 * │  G ─────────────────── 128                │
 * │  B ─────────────────── 0                  │
 * │  Opacity ────────────── 255               │
 * ├───── 🔔 Alerts ────────────────────────────│  sectionY[1]
 * │  [✔ Particle Alerts]   [✔ Sound]          │
 * │  [✔ Screen Flash]                         │
 * │  R / G / B sliders (alert color)          │
 * │  Sensitivity ─────────── 20               │
 * ├───── 🖥 Display ────────────────────────────│  sectionY[2]
 * │  [✔ Show Cast Count]                      │
 * ├───────────────────────────────────────────┤
 * │  [Save]   [Reset Stats]   [Close]         │
 * └───────────────────────────────────────────┘
 * </pre>
 */
public final class GuiEnoughFishing extends GuiScreen {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int W        = 380;
    private static final int H        = 470;
    private static final int PAD      = 12;
    private static final int SLIDER_W = 220;
    private static final int SLIDER_H = 12;
    private static final int BTN_H    = 20;
    private static final int BTN_W    = 150;

    // ── Button IDs ────────────────────────────────────────────────────────────
    private static final int ID_TIMER_EN   = 10;
    private static final int ID_SHOW_MS    = 11;
    private static final int ID_ALERT_EN   = 12;
    private static final int ID_SOUND      = 13;
    private static final int ID_FLASH      = 14;
    private static final int ID_CAST_CNT   = 15;
    private static final int ID_SAVE       = 20;
    private static final int ID_RESET      = 21;
    private static final int ID_CLOSE      = 22;

    // ── State ─────────────────────────────────────────────────────────────────
    private int px, py;                    // panel top-left
    private final List<MEFSlider> sliders = new ArrayList<>();

    // Timer color sliders
    private MEFSlider slTimerR, slTimerG, slTimerB, slOpacity;
    // Alert color sliders
    private MEFSlider slAlertR, slAlertG, slAlertB, slSensitivity;

    // Live color previews (packed RGB, updated every frame)
    private int previewTimerColor;
    private int previewAlertColor;

    // ── GuiScreen lifecycle ───────────────────────────────────────────────────

    @Override
    public void initGui() {
        sliders.clear();
        buttonList.clear();

        Config cfg = EnoughFishing.INSTANCE.getConfig();

        px = (width  - W) / 2;
        py = (height - H) / 2;

        // ── TIMER SECTION ─────────────────────────────────────────────────────
        int y = py + 32;

        addBtn(new ToggleButton(ID_TIMER_EN, px + PAD,           y, BTN_W, BTN_H, "Timer",   cfg.isTimerEnabled()));
        addBtn(new ToggleButton(ID_SHOW_MS,  px + PAD + BTN_W + 8, y, BTN_W, BTN_H, "Show ms", cfg.isShowMilliseconds()));
        y += BTN_H + 10;

        slTimerR = addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "R", ColorUtils.getRed(cfg.getTimerColor()),   0, 255)); y += SLIDER_H + 6;
        slTimerG = addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "G", ColorUtils.getGreen(cfg.getTimerColor()), 0, 255)); y += SLIDER_H + 6;
        slTimerB = addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "B", ColorUtils.getBlue(cfg.getTimerColor()),  0, 255)); y += SLIDER_H + 6;
        slOpacity= addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "Opacity", (int)(cfg.getHudOpacity() * 255f), 0, 255)); y += SLIDER_H + 14;

        // ── ALERTS SECTION ────────────────────────────────────────────────────
        addBtn(new ToggleButton(ID_ALERT_EN, px + PAD,           y, BTN_W, BTN_H, "Particle Alerts", cfg.isParticleAlertsEnabled()));
        addBtn(new ToggleButton(ID_SOUND,    px + PAD + BTN_W + 8, y, BTN_W, BTN_H, "Sound Alert",  cfg.isSoundAlertEnabled()));
        y += BTN_H + 6;

        addBtn(new ToggleButton(ID_FLASH, px + PAD, y, BTN_W, BTN_H, "Screen Flash", cfg.isScreenFlashEnabled()));
        y += BTN_H + 10;

        slAlertR     = addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "R", ColorUtils.getRed(cfg.getAlertColor()),   0, 255)); y += SLIDER_H + 6;
        slAlertG     = addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "G", ColorUtils.getGreen(cfg.getAlertColor()), 0, 255)); y += SLIDER_H + 6;
        slAlertB     = addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "B", ColorUtils.getBlue(cfg.getAlertColor()),  0, 255)); y += SLIDER_H + 6;
        // Sensitivity: 1.0–5.0 stored as 10–50 for integer precision
        slSensitivity= addSlider(new MEFSlider(px + PAD, y, SLIDER_W, SLIDER_H, "Sensitivity/10", (int)(cfg.getParticleSensitivity() * 10f), 10, 50)); y += SLIDER_H + 14;

        // ── DISPLAY SECTION ───────────────────────────────────────────────────
        addBtn(new ToggleButton(ID_CAST_CNT, px + PAD, y, BTN_W, BTN_H, "Show Cast Count", cfg.isShowCastCount()));

        // ── BOTTOM BUTTONS ────────────────────────────────────────────────────
        int bottomY = py + H - BTN_H - PAD;
        addBtn(new GuiButton(ID_SAVE,  px + PAD,            bottomY, 100, BTN_H, "§aSave"));
        addBtn(new GuiButton(ID_RESET, px + PAD + 108,      bottomY, 120, BTN_H, "§eReset Stats"));
        addBtn(new GuiButton(ID_CLOSE, px + W - 110,        bottomY, 100, BTN_H, "§cClose"));

        refreshColorPreviews();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // ── Panel ─────────────────────────────────────────────────────────────
        RenderUtils.drawRect(px,      py,      px + W,      py + H,      0xE5101018);
        RenderUtils.drawRect(px,      py,      px + W,      py + 24,     0xFF1A1A30);
        RenderUtils.drawRect(px,      py + 24, px + W,      py + 25,     0xFF3333AA);

        drawCenteredString(fontRendererObj,
            "§b§lEnough Fishing  §8v" + EnoughFishing.VERSION,
            px + W / 2, py + 8, 0xFFFFFF);

        // ── Section headers ───────────────────────────────────────────────────
        int y = py + 30;
        drawSection("⏱ Timer",   y);  y += BTN_H + 10 + (SLIDER_H + 6) * 3 + SLIDER_H + 14 + 6;
        drawSection("🔔 Alerts",  y);  y += BTN_H + 6 + BTN_H + 10 + (SLIDER_H + 6) * 3 + SLIDER_H + 14 + 6;
        drawSection("🖥 Display", y);

        // ── Color preview swatches ────────────────────────────────────────────
        int swatchX = px + PAD + SLIDER_W + 55;
        int timerSwatchY = py + 32 + BTN_H + 10;
        int alertSwatchY = py + 32 + BTN_H + 10 + (SLIDER_H + 6) * 3 + SLIDER_H + 14 + (BTN_H + 6) * 2 + BTN_H + 10;

        drawColorSwatch(previewTimerColor, swatchX, timerSwatchY, 20, (SLIDER_H + 6) * 3 + SLIDER_H);
        drawColorSwatch(previewAlertColor, swatchX, alertSwatchY,  20, (SLIDER_H + 6) * 3 + SLIDER_H);

        // ── Sliders ───────────────────────────────────────────────────────────
        for (MEFSlider sl : sliders) {
            sl.draw(fontRendererObj, mouseX, mouseY);
        }

        // ── Buttons (must come after custom elements so they render on top) ──
        super.drawScreen(mouseX, mouseY, partialTicks);

        refreshColorPreviews();
    }

    // ── Mouse events ──────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            for (MEFSlider sl : sliders) sl.mousePressed(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSince) {
        super.mouseClickMove(mouseX, mouseY, button, timeSince);
        if (button == 0) {
            for (MEFSlider sl : sliders) sl.mouseDragged(mouseX);
        }
        refreshColorPreviews();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for (MEFSlider sl : sliders) sl.mouseReleased();
    }

    // ── Button actions ────────────────────────────────────────────────────────

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn instanceof ToggleButton) {
            ToggleButton tb = (ToggleButton) btn;
            tb.toggle();
            Config cfg = EnoughFishing.INSTANCE.getConfig();
            switch (btn.id) {
                case ID_TIMER_EN:  cfg.setTimerEnabled(tb.isEnabled());           break;
                case ID_SHOW_MS:   cfg.setShowMilliseconds(tb.isEnabled());       break;
                case ID_ALERT_EN:  cfg.setParticleAlertsEnabled(tb.isEnabled());  break;
                case ID_SOUND:     cfg.setSoundAlertEnabled(tb.isEnabled());      break;
                case ID_FLASH:     cfg.setScreenFlashEnabled(tb.isEnabled());     break;
                case ID_CAST_CNT:  cfg.setShowCastCount(tb.isEnabled());          break;
            }
        } else {
            switch (btn.id) {
                case ID_SAVE:
                    applyAndSave();
                    mc.displayGuiScreen(null);
                    break;
                case ID_RESET:
                    FishingTracker.INSTANCE.resetStats();
                    break;
                case ID_CLOSE:
                    mc.displayGuiScreen(null);
                    break;
            }
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyAndSave() {
        Config cfg = EnoughFishing.INSTANCE.getConfig();
        cfg.setTimerColor(ColorUtils.fromRGB(slTimerR.getValue(), slTimerG.getValue(), slTimerB.getValue()));
        cfg.setHudOpacity(slOpacity.getValue() / 255f);
        cfg.setAlertColor(ColorUtils.fromRGB(slAlertR.getValue(), slAlertG.getValue(), slAlertB.getValue()));
        cfg.setParticleSensitivity(slSensitivity.getValue() / 10f);
        cfg.save();
    }

    private void refreshColorPreviews() {
        previewTimerColor = ColorUtils.fromRGB(slTimerR.getValue(), slTimerG.getValue(), slTimerB.getValue());
        previewAlertColor = ColorUtils.fromRGB(slAlertR.getValue(), slAlertG.getValue(), slAlertB.getValue());
    }

    private void drawSection(String title, int y) {
        RenderUtils.drawRect(px + PAD, y, px + W - PAD, y + 1, 0xFF3344AA);
        drawString(fontRendererObj, "§7" + title, px + PAD, y - 9, 0xFFFFFF);
    }

    private void drawColorSwatch(int rgb, int x, int y, int size, int totalH) {
        // Border
        RenderUtils.drawRect(x - 1, y - 1, x + size + 1, y + totalH + 1, 0xFF555566);
        // Checkerboard pattern to show through transparency
        RenderUtils.drawRect(x, y,             x + size / 2, y + totalH / 2, 0xFF888888);
        RenderUtils.drawRect(x + size / 2, y + totalH / 2, x + size, y + totalH, 0xFF888888);
        // Color fill
        RenderUtils.drawRect(x, y, x + size, y + totalH, 0xFF000000 | rgb);
    }

    /** Convenience: add a GuiButton and return it. */
    @SuppressWarnings("unchecked")
    private <T extends GuiButton> T addBtn(T btn) {
        buttonList.add(btn);
        return btn;
    }

    /** Convenience: register a MEFSlider and return it. */
    private MEFSlider addSlider(MEFSlider sl) {
        sliders.add(sl);
        return sl;
    }
}
