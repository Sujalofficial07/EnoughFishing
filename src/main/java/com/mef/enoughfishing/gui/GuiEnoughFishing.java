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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration screen — neon green / purple professional theme.
 *
 * Layout (top-down):
 *  ┌──────────────────── TITLE ────────────────────┐
 *  │  ◈ ENOUGH FISHING  v1.0.0                     │
 *  ╠══════════════ ⏱ TIMER ════════════════════════╣
 *  │  [✔ Timer]  [✔ Show ms]  [✔ Rainbow]          │
 *  │  R ───────────────  G ───────────────          │
 *  │  B ───────────────  Opacity ─────────          │
 *  │  ████ color swatch                             │
 *  ╠══════════════ 🔔 ALERTS ══════════════════════╣
 *  │  [✔ Particle Alerts]  [✔ Sound]  [✔ Flash]    │
 *  │  R / G / B ───────  Sensitivity ───────        │
 *  │  ████ color swatch                             │
 *  ╠══════════════ 🖥 DISPLAY ══════════════════════╣
 *  │  [✔ Show Cast Count]                           │
 *  ╠═══════════════════════════════════════════════╣
 *  │  [ §a SAVE ]    [ §e RESET STATS ]  [ §c X ]  │
 *  └───────────────────────────────────────────────┘
 */
public final class GuiEnoughFishing extends GuiScreen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int W        = 390;
    private static final int H        = 505;
    private static final int PAD      = 13;
    private static final int SL_W     = 160;  // slider track width
    private static final int SL_H     = 14;
    private static final int BTN_W    = 120;
    private static final int BTN_H    = 18;
    private static final int COL_GAP  = 12;   // gap between two-column rows

    // ── Neon palette ──────────────────────────────────────────────────────────
    private static final int BG_OUTER    = 0xF5040310;  // near-black
    private static final int BG_PANEL    = 0xF2130828;  // dark purple panel
    private static final int BG_HEADER   = 0xFF090422;  // deepest purple header
    private static final int GLOW_COLOR  = 0xFF7722BB;  // purple glow
    private static final int SECTION_LINE= 0xFF3D1270;  // section divider
    private static final int NEON_GREEN  = 0xFF00FF88;
    private static final int NEON_PURPLE = 0xFF9D4EDD;
    private static final int TXT_PRIMARY = 0xFF00FF88;  // neon green
    private static final int TXT_SECOND  = 0xFFBB88FF;  // soft lavender
    private static final int TXT_DIM     = 0xFF5C3B7A;

    // ── Button IDs ────────────────────────────────────────────────────────────
    private static final int ID_TIMER_EN  = 10, ID_SHOW_MS = 11, ID_RAINBOW = 12;
    private static final int ID_ALERT_EN  = 13, ID_SOUND   = 14, ID_FLASH   = 15;
    private static final int ID_CAST_CNT  = 16;
    private static final int ID_SAVE      = 20, ID_RESET   = 21, ID_CLOSE   = 22;

    // ── Runtime state ─────────────────────────────────────────────────────────
    private int px, py;
    private final List<MEFSlider> sliders = new ArrayList<>();

    // Timer color sliders
    private MEFSlider slTimerR, slTimerG, slTimerB, slOpacity;
    // Alert color sliders
    private MEFSlider slAlertR, slAlertG, slAlertB, slSensitivity;

    // Live color preview (packed RGB, refreshed every frame)
    private int previewTimer, previewAlert;

    // Stored Y positions for section headers (set in initGui, read in drawScreen)
    private int ySecTimer, ySecAlerts, ySecDisplay, yBtnRow;

    // ── GuiScreen lifecycle ───────────────────────────────────────────────────

    @Override
    public void initGui() {
        sliders.clear();
        buttonList.clear();

        Config cfg = EnoughFishing.INSTANCE.getConfig();
        px = (width  - W) / 2;
        py = (height - H) / 2;

        int y = py + 30;  // start below the title bar

        // ── TIMER section ─────────────────────────────────────────────────────
        ySecTimer = y;
        y += 14;  // section header height

        // Row 1: three toggle buttons
        int col2 = px + PAD + BTN_W + COL_GAP;
        int col3 = col2 + BTN_W + COL_GAP;
        addBtn(new ToggleButton(ID_TIMER_EN, px + PAD, y, BTN_W, BTN_H, "Timer",   cfg.isTimerEnabled()));
        addBtn(new ToggleButton(ID_SHOW_MS,  col2,     y, BTN_W, BTN_H, "Show ms", cfg.isShowMilliseconds()));
        addBtn(new ToggleButton(ID_RAINBOW,  col3,     y, BTN_W, BTN_H, "Rainbow", cfg.isRainbowMode()));
        y += BTN_H + 10;

        // Row 2: R/G sliders side by side
        int rCol = px + PAD;
        int gCol = px + PAD + SL_W + 60 + COL_GAP;  // 60 = label space
        slTimerR = addSlider(new MEFSlider(rCol, y, SL_W, SL_H, "R", ColorUtils.getRed(cfg.getTimerColor()),   0, 255));
        slTimerG = addSlider(new MEFSlider(gCol, y, SL_W, SL_H, "G", ColorUtils.getGreen(cfg.getTimerColor()), 0, 255));
        y += SL_H + 8;

        // Row 3: B/Opacity sliders side by side
        slTimerB  = addSlider(new MEFSlider(rCol, y, SL_W, SL_H, "B",       ColorUtils.getBlue(cfg.getTimerColor()),   0, 255));
        slOpacity = addSlider(new MEFSlider(gCol, y, SL_W, SL_H, "Opacity", (int)(cfg.getHudOpacity() * 255f), 0, 255));
        y += SL_H + 14;

        // ── ALERTS section ────────────────────────────────────────────────────
        ySecAlerts = y;
        y += 14;

        addBtn(new ToggleButton(ID_ALERT_EN, px + PAD, y, BTN_W, BTN_H, "Alerts",      cfg.isParticleAlertsEnabled()));
        addBtn(new ToggleButton(ID_SOUND,    col2,     y, BTN_W, BTN_H, "Sound",        cfg.isSoundAlertEnabled()));
        addBtn(new ToggleButton(ID_FLASH,    col3,     y, BTN_W, BTN_H, "Screen Flash", cfg.isScreenFlashEnabled()));
        y += BTN_H + 10;

        slAlertR      = addSlider(new MEFSlider(rCol, y, SL_W, SL_H, "R", ColorUtils.getRed(cfg.getAlertColor()),   0, 255));
        slAlertG      = addSlider(new MEFSlider(gCol, y, SL_W, SL_H, "G", ColorUtils.getGreen(cfg.getAlertColor()), 0, 255));
        y += SL_H + 8;

        slAlertB      = addSlider(new MEFSlider(rCol, y, SL_W, SL_H, "B",           ColorUtils.getBlue(cfg.getAlertColor()),             0, 255));
        slSensitivity = addSlider(new MEFSlider(gCol, y, SL_W, SL_H, "Radius×10",   (int)(cfg.getParticleSensitivity() * 10f), 10, 50));
        y += SL_H + 14;

        // ── DISPLAY section ───────────────────────────────────────────────────
        ySecDisplay = y;
        y += 14;

        addBtn(new ToggleButton(ID_CAST_CNT, px + PAD, y, BTN_W, BTN_H, "Cast Count", cfg.isShowCastCount()));
        y += BTN_H + 14;

        // ── Action buttons ────────────────────────────────────────────────────
        yBtnRow = y;
        int bw = 105;
        addBtn(new GuiButton(ID_SAVE,  px + PAD,               y, bw, 22, "SAVE"));
        addBtn(new GuiButton(ID_RESET, px + PAD + bw + 8,      y, bw, 22, "RESET STATS"));
        addBtn(new GuiButton(ID_CLOSE, px + W - bw - PAD,      y, bw, 22, "CLOSE"));

        refreshColorPreviews();
    }

    // ── drawScreen ────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        drawPanel();
        drawSections();
        drawColorSwatches();

        for (MEFSlider sl : sliders) sl.draw(fontRendererObj, mx, my);

        // Buttons on top of everything else
        super.drawScreen(mx, my, pt);

        // Action button neon tint overlays (drawn after super so they're on top)
        tintActionButton(ID_SAVE,  NEON_GREEN,  mx, my);
        tintActionButton(ID_RESET, 0xFFFFCC00,  mx, my);
        tintActionButton(ID_CLOSE, 0xFFFF4455,  mx, my);

        refreshColorPreviews();
    }

    // ── Panel ─────────────────────────────────────────────────────────────────

    private void drawPanel() {
        // Multi-layer glow border (outer → inner)
        RenderUtils.drawRect(px - 4, py - 4, px + W + 4, py + H + 4, 0x10B350FF);
        RenderUtils.drawRect(px - 3, py - 3, px + W + 3, py + H + 3, 0x20B350FF);
        RenderUtils.drawRect(px - 2, py - 2, px + W + 2, py + H + 2, 0x409D4EDD);
        RenderUtils.drawRect(px - 1, py - 1, px + W + 1, py + H + 1, 0x70852BBB);

        // Panel fill
        RenderUtils.drawRect(px, py, px + W, py + H, BG_PANEL);

        // Title bar
        RenderUtils.drawRect(px, py, px + W, py + 26, BG_HEADER);

        // Title bar bottom glow line
        RenderUtils.drawRect(px, py + 26, px + W, py + 27, GLOW_COLOR);
        RenderUtils.drawRect(px, py + 27, px + W, py + 28, GLOW_COLOR & 0x55FFFFFF);

        // Title text
        String title = "\u25c8  ENOUGH FISHING  \u25c8";
        int    tw    = fontRendererObj.getStringWidth(title);
        fontRendererObj.drawStringWithShadow(title, px + (W - tw) / 2f, py + 9f, NEON_GREEN);

        // Version tag (top right)
        String ver = "v" + EnoughFishing.VERSION;
        fontRendererObj.drawStringWithShadow(ver, px + W - fontRendererObj.getStringWidth(ver) - PAD, py + 9f, TXT_DIM);
    }

    // ── Section headers ───────────────────────────────────────────────────────

    private void drawSections() {
        drawSectionHeader("\u231a TIMER",   ySecTimer);
        drawSectionHeader("\u25ce ALERTS",  ySecAlerts);
        drawSectionHeader("\u25a3 DISPLAY", ySecDisplay);
    }

    private void drawSectionHeader(String label, int y) {
        // Full-width horizontal rule
        RenderUtils.drawRect(px + PAD, y + 10, px + W - PAD, y + 11, SECTION_LINE);
        RenderUtils.drawRect(px + PAD, y + 11, px + W - PAD, y + 12, SECTION_LINE & 0x55FFFFFF);

        // Label chip (small colored pill)
        int lw = fontRendererObj.getStringWidth(label) + 8;
        RenderUtils.drawRect(px + PAD, y, px + PAD + lw, y + 11, BG_PANEL); // erase rule under label
        fontRendererObj.drawStringWithShadow(label, px + PAD + 4f, y + 1f, NEON_PURPLE);
    }

    // ── Color swatches ────────────────────────────────────────────────────────

    private void drawColorSwatches() {
        // Timer swatch — to the right of the slider columns
        int swatchX = px + W - PAD - 24;
        int timerSwatchY = ySecTimer + 14 + BTN_H + 10;
        int alertSwatchY = ySecAlerts + 14 + BTN_H + 10;
        int swatchH = (SL_H + 8) + SL_H;  // spans two slider rows

        drawSwatch(previewTimer, swatchX, timerSwatchY, 22, swatchH);
        drawSwatch(previewAlert, swatchX, alertSwatchY, 22, swatchH);
    }

    private void drawSwatch(int rgb, int x, int y, int w, int h) {
        // Outer border glow
        RenderUtils.drawRect(x - 2, y - 2, x + w + 2, y + h + 2, 0x30FFFFFF);
        // Checkerboard hint (dark gray squares for transparency reference)
        RenderUtils.drawRect(x, y,         x + w / 2, y + h / 2, 0xFF2A2A2A);
        RenderUtils.drawRect(x + w / 2, y + h / 2, x + w, y + h, 0xFF2A2A2A);
        RenderUtils.drawRect(x + w / 2, y,         x + w, y + h / 2, 0xFF222222);
        RenderUtils.drawRect(x, y + h / 2, x + w / 2, y + h,         0xFF222222);
        // Color fill (full alpha)
        RenderUtils.drawRect(x, y, x + w, y + h, 0xFF000000 | rgb);
        // Crisp 1px border
        RenderUtils.drawBorder(x, y, x + w, y + h, 0xFF7722BB);
    }

    // ── Action button neon tint ───────────────────────────────────────────────

    private void tintActionButton(int id, int neonColor, int mx, int my) {
        for (Object o : buttonList) {
            if (!(o instanceof GuiButton)) continue;
            GuiButton btn = (GuiButton) o;
            if (btn.id != id || btn instanceof ToggleButton) continue;

            boolean hovered = mx >= btn.xPosition && my >= btn.yPosition
                           && mx < btn.xPosition + btn.width
                           && my < btn.yPosition + btn.height;

            // Colored border
            RenderUtils.drawBorder(btn.xPosition, btn.yPosition,
                btn.xPosition + btn.width, btn.yPosition + btn.height, neonColor);
            // Faint hover fill
            if (hovered) {
                RenderUtils.drawRect(btn.xPosition + 1, btn.yPosition + 1,
                    btn.xPosition + btn.width - 1, btn.yPosition + btn.height - 1,
                    neonColor & 0x33FFFFFF);
            }
            // Relabel with neon color
            String lbl;
            switch (id) {
                case ID_SAVE:  lbl = "SAVE";        break;
                case ID_RESET: lbl = "RESET STATS"; break;
                default:       lbl = "CLOSE";       break;
            }
            int tx = btn.xPosition + btn.width  / 2 - fontRendererObj.getStringWidth(lbl) / 2;
            int ty = btn.yPosition + btn.height / 2 - fontRendererObj.FONT_HEIGHT / 2;
            fontRendererObj.drawStringWithShadow(lbl, tx, ty, neonColor);
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        if (btn == 0) for (MEFSlider sl : sliders) sl.mousePressed(mx, my);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long timeSince) {
        super.mouseClickMove(mx, my, btn, timeSince);
        if (btn == 0) for (MEFSlider sl : sliders) sl.mouseDragged(mx);
        refreshColorPreviews();
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        super.mouseReleased(mx, my, state);
        for (MEFSlider sl : sliders) sl.mouseReleased();
    }

    // ── Button actions ────────────────────────────────────────────────────────

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        Config cfg = EnoughFishing.INSTANCE.getConfig();

        if (btn instanceof ToggleButton) {
            ToggleButton tb = (ToggleButton) btn;
            tb.toggle();
            switch (btn.id) {
                case ID_TIMER_EN:  cfg.setTimerEnabled(tb.isEnabled());           break;
                case ID_SHOW_MS:   cfg.setShowMilliseconds(tb.isEnabled());       break;
                case ID_RAINBOW:   cfg.setRainbowMode(tb.isEnabled());            break;
                case ID_ALERT_EN:  cfg.setParticleAlertsEnabled(tb.isEnabled());  break;
                case ID_SOUND:     cfg.setSoundAlertEnabled(tb.isEnabled());      break;
                case ID_FLASH:     cfg.setScreenFlashEnabled(tb.isEnabled());     break;
                case ID_CAST_CNT:  cfg.setShowCastCount(tb.isEnabled());          break;
            }
        } else {
            switch (btn.id) {
                case ID_SAVE:
                    applySliders();
                    cfg.save();
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

    private void applySliders() {
        Config cfg = EnoughFishing.INSTANCE.getConfig();
        cfg.setTimerColor(ColorUtils.fromRGB(slTimerR.getValue(), slTimerG.getValue(), slTimerB.getValue()));
        cfg.setHudOpacity(slOpacity.getValue() / 255f);
        cfg.setAlertColor(ColorUtils.fromRGB(slAlertR.getValue(), slAlertG.getValue(), slAlertB.getValue()));
        cfg.setParticleSensitivity(slSensitivity.getValue() / 10f);
    }

    private void refreshColorPreviews() {
        previewTimer = ColorUtils.fromRGB(slTimerR.getValue(), slTimerG.getValue(), slTimerB.getValue());
        previewAlert = ColorUtils.fromRGB(slAlertR.getValue(), slAlertG.getValue(), slAlertB.getValue());
    }

    @SuppressWarnings("unchecked")
    private <T extends GuiButton> T addBtn(T b) { buttonList.add(b); return b; }
    private MEFSlider addSlider(MEFSlider s)     { sliders.add(s);   return s; }
}
