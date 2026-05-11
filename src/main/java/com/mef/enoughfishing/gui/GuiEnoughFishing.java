package com.mef.enoughfishing.gui;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.gui.elements.MEFSlider;
import com.mef.enoughfishing.gui.elements.ToggleSwitch;
import com.mef.enoughfishing.utils.ColorUtils;
import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Tabbed configuration GUI — Lunar Client aesthetic.
 *
 * Structure:
 *  ┌──────────────────────────── Header (32px) ─────────────────────────────┐
 *  │  Enough Fishing  v1.0.0                                           [×]  │
 *  ├─────────────────────────── Tab bar (26px) ─────────────────────────────┤
 *  │     [Timer]           [Alerts]           [Display]                     │
 *  │      ─────                                                             │ ← purple underline
 *  ├──────────────────────── Content area (var) ─────────────────────────────┤
 *  │  ── Timer Settings ───────────────────────────────────────────────     │
 *  │  Timer Enabled                                            [·····●]     │
 *  │  ...                                                                   │
 *  ├────────────────────────── Footer (36px) ────────────────────────────────┤
 *  │  [ Save ]   [ Reset Stats ]                              [ Close ]     │
 *  └─────────────────────────────────────────────────────────────────────────┘
 *
 * No GuiButton subclass used — all controls are custom components
 * (ToggleSwitch, MEFSlider) with manual mouse routing. This gives us
 * complete visual control without fighting Minecraft's button chrome.
 */
public final class GuiEnoughFishing extends GuiScreen {

    // ── Panel ─────────────────────────────────────────────────────────────────
    private static final int W  = 340;
    private static final int H  = 460;
    private static final int PAD = 12;

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int HEADER_H = 32;
    private static final int TAB_H    = 26;
    private static final int FOOTER_H = 36;
    /** Y offset from panel top to first content row. */
    private static final int CONT_OFF = HEADER_H + TAB_H + 8;
    /** Pixels consumed by a section header (label + breathing room). */
    private static final int SEC_H    = 18;
    /** Unified row height for both ToggleSwitch and MEFSlider. */
    private static final int ROW      = 28;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG        = 0xF20B0C16;
    private static final int C_PANEL     = 0xF2101122;
    private static final int C_HEADER    = 0xFF080914;
    private static final int C_FOOTER    = 0xFF080914;
    private static final int C_OUTLINE   = 0xFF1C1D32;
    private static final int C_DIVIDER   = 0xFF1E2036;
    private static final int C_SEC_TEXT  = 0xFF606080;
    private static final int C_TEXT_1    = 0xFFDEDEF4;
    private static final int C_TEXT_2    = 0xFF6868A0;
    private static final int C_CLOSE_HOV = 0xFFFF4466;
    private static final int C_TAB_ACT   = 0xFFE0E0F8;
    private static final int C_TAB_IDLE  = 0xFF484870;
    private static final int C_TAB_LINE  = 0xFF6B4FE0;
    private static final int C_TAB_HOVER = 0x0AFFFFFF;

    // ── Button palette ────────────────────────────────────────────────────────
    private static final int C_SAVE_BG  = 0xFF0E2C1C, C_SAVE_BD  = 0xFF004E2C, C_SAVE_TX  = 0xFF00D87E;
    private static final int C_RESET_BG = 0xFF1E1A08, C_RESET_BD = 0xFF443C00, C_RESET_TX = 0xFFD4AA00;
    private static final int C_CLOSE_BG = 0xFF200C1C, C_CLOSE_BD = 0xFF540C40, C_CLOSE_TX = 0xFFCC0066;

    // ── Tab IDs ───────────────────────────────────────────────────────────────
    private static final int TAB_TIMER   = 0;
    private static final int TAB_ALERTS  = 1;
    private static final int TAB_DISPLAY = 2;
    private static final String[] TAB_LABELS = {"Timer", "Alerts", "Display"};

    // ── Toggle IDs ────────────────────────────────────────────────────────────
    private static final int ID_TIMER_EN = 1, ID_SHOW_MS = 2, ID_RAINBOW  = 3;
    private static final int ID_ALERT_EN = 4, ID_SOUND   = 5, ID_FLASH    = 6;
    private static final int ID_CAST_CNT = 7;

    // ── Runtime ───────────────────────────────────────────────────────────────
    private int px, py, activeTab;

    // Timer tab
    private ToggleSwitch tsTimerEn, tsShowMs, tsRainbow;
    private MEFSlider    slTimerR, slTimerG, slTimerB, slOpacity;
    // Pre-allocated slider array — avoids allocation in mouseDragged (called ~60/s)
    private MEFSlider[]  timerSliders;

    // Alerts tab
    private ToggleSwitch tsAlertEn, tsSoundAlert, tsFlash;
    private MEFSlider    slApprR,  slApprG,  slApprB;
    private MEFSlider    slArrvR,  slArrvG,  slArrvB;
    private MEFSlider    slSens;
    private MEFSlider[]  alertSliders;

    // Display tab
    private ToggleSwitch tsCastCnt;

    // Footer button areas [x, y, w, h]
    private int[] bSave, bReset, bClose;

    // Color preview values (packed RGB, refreshed each frame)
    private int pTimer, pAppr, pArrv;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void initGui() {
        buttonList.clear();   // no vanilla buttons, but keep contract
        Config cfg = EnoughFishing.INSTANCE.getConfig();

        px = (width  - W) / 2;
        py = (height - H) / 2;

        int cx = px + PAD;
        int cw = W - PAD * 2;
        int cs = py + CONT_OFF;   // content start Y

        // ── Timer tab ─────────────────────────────────────────────────────────
        tsTimerEn = new ToggleSwitch(ID_TIMER_EN, cx, cs,          cw, "Timer Enabled",    cfg.isTimerEnabled());
        tsShowMs  = new ToggleSwitch(ID_SHOW_MS,  cx, cs + ROW,    cw, "Show Centiseconds", cfg.isShowMilliseconds());
        tsRainbow = new ToggleSwitch(ID_RAINBOW,  cx, cs + ROW*2,  cw, "Rainbow Color",     cfg.isRainbowMode());

        int tcY = cs + ROW*3 + SEC_H;
        slTimerR = new MEFSlider(cx, tcY,         cw, "R", ColorUtils.getRed  (cfg.getTimerColor()), 0, 255);
        slTimerG = new MEFSlider(cx, tcY + ROW,   cw, "G", ColorUtils.getGreen(cfg.getTimerColor()), 0, 255);
        slTimerB = new MEFSlider(cx, tcY + ROW*2, cw, "B", ColorUtils.getBlue (cfg.getTimerColor()), 0, 255);

        int opY = tcY + ROW*3 + SEC_H;
        slOpacity = new MEFSlider(cx, opY, cw, "Opacity", (int)(cfg.getHudOpacity() * 255f), 0, 255);

        timerSliders = new MEFSlider[]{slTimerR, slTimerG, slTimerB, slOpacity};

        // ── Alerts tab ────────────────────────────────────────────────────────
        tsAlertEn   = new ToggleSwitch(ID_ALERT_EN, cx, cs,         cw, "Particle Alerts", cfg.isParticleAlertsEnabled());
        tsSoundAlert= new ToggleSwitch(ID_SOUND,    cx, cs + ROW,   cw, "Sound on Bite",   cfg.isSoundAlertEnabled());
        tsFlash     = new ToggleSwitch(ID_FLASH,    cx, cs + ROW*2, cw, "Screen Flash",    cfg.isScreenFlashEnabled());

        int acY = cs + ROW*3 + SEC_H;
        slApprR = new MEFSlider(cx, acY,         cw, "R", ColorUtils.getRed  (cfg.getApproachingColor()), 0, 255);
        slApprG = new MEFSlider(cx, acY + ROW,   cw, "G", ColorUtils.getGreen(cfg.getApproachingColor()), 0, 255);
        slApprB = new MEFSlider(cx, acY + ROW*2, cw, "B", ColorUtils.getBlue (cfg.getApproachingColor()), 0, 255);

        int rvY = acY + ROW*3 + SEC_H;
        slArrvR = new MEFSlider(cx, rvY,         cw, "R", ColorUtils.getRed  (cfg.getArrivedColor()), 0, 255);
        slArrvG = new MEFSlider(cx, rvY + ROW,   cw, "G", ColorUtils.getGreen(cfg.getArrivedColor()), 0, 255);
        slArrvB = new MEFSlider(cx, rvY + ROW*2, cw, "B", ColorUtils.getBlue (cfg.getArrivedColor()), 0, 255);

        int snY = rvY + ROW*3 + SEC_H;
        slSens = new MEFSlider(cx, snY, cw, "Radius×10", (int)(cfg.getParticleSensitivity() * 10f), 10, 50);

        alertSliders = new MEFSlider[]{slApprR, slApprG, slApprB, slArrvR, slArrvG, slArrvB, slSens};

        // ── Display tab ───────────────────────────────────────────────────────
        tsCastCnt = new ToggleSwitch(ID_CAST_CNT, cx, cs, cw, "Show Cast Count", cfg.isShowCastCount());

        // ── Footer button bounds ───────────────────────────────────────────────
        int fy = py + H - FOOTER_H + 8;
        bSave  = new int[]{px + PAD,       fy, 80, 20};
        bReset = new int[]{px + PAD + 88,  fy, 92, 20};
        bClose = new int[]{px + W - PAD - 76, fy, 76, 20};

        refreshPreviews();
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();

        // Subtle outer glow (1px, 20% alpha purple)
        RenderUtils.drawRect(px - 1, py - 1, px + W + 1, py + H + 1, 0x304A3ACC);

        // Panel
        RenderUtils.drawRect(px, py, px + W, py + H, C_PANEL);

        // Header
        RenderUtils.drawRect(px, py, px + W, py + HEADER_H, C_HEADER);
        RenderUtils.drawRect(px, py + HEADER_H, px + W, py + HEADER_H + 1, C_OUTLINE);
        drawHeader(mx, my);

        // Tab bar
        RenderUtils.drawRect(px, py + HEADER_H + TAB_H, px + W, py + HEADER_H + TAB_H + 1, C_OUTLINE);
        drawTabs(mx, my);

        // Content (delegates to active tab)
        drawContent(mx, my);

        // Footer
        int fy = py + H - FOOTER_H;
        RenderUtils.drawRect(px, fy, px + W, fy + 1, C_OUTLINE);
        RenderUtils.drawRect(px, fy + 1, px + W, py + H, C_FOOTER);
        drawFooterBtn(bSave,  "Save",       C_SAVE_BG,  C_SAVE_BD,  C_SAVE_TX,  mx, my);
        drawFooterBtn(bReset, "Reset Stats",C_RESET_BG, C_RESET_BD, C_RESET_TX, mx, my);
        drawFooterBtn(bClose, "Close",      C_CLOSE_BG, C_CLOSE_BD, C_CLOSE_TX, mx, my);

        refreshPreviews();
        super.drawScreen(mx, my, pt);  // compat (no vanilla buttons)
    }

    private void drawHeader(int mx, int my) {
        String title = "Enough Fishing";
        fontRendererObj.drawString(title, px + PAD, py + (HEADER_H - fontRendererObj.FONT_HEIGHT) / 2, C_TEXT_1);

        String ver = "v" + EnoughFishing.VERSION;
        int    vx  = px + PAD + fontRendererObj.getStringWidth(title) + 7;
        fontRendererObj.drawString(ver, vx, py + (HEADER_H - fontRendererObj.FONT_HEIGHT) / 2, C_TEXT_2);

        // [×] close button
        boolean xHov = mx >= px + W - 24 && mx < px + W - 8
                    && my >= py + 8      && my < py + 24;
        fontRendererObj.drawString("×", px + W - 20, py + (HEADER_H - fontRendererObj.FONT_HEIGHT) / 2,
                                   xHov ? C_CLOSE_HOV : C_TEXT_2);
    }

    private void drawTabs(int mx, int my) {
        int tabBarY = py + HEADER_H;
        int tabW    = W / TAB_LABELS.length;
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int tx  = px + i * tabW;
            boolean active  = activeTab == i;
            boolean tabHov  = !active && mx >= tx && mx < tx + tabW
                                      && my >= tabBarY && my < tabBarY + TAB_H;
            if (tabHov) RenderUtils.drawRect(tx, tabBarY, tx + tabW, tabBarY + TAB_H, C_TAB_HOVER);

            String lbl  = TAB_LABELS[i];
            int    lx   = tx + (tabW - fontRendererObj.getStringWidth(lbl)) / 2;
            int    ly   = tabBarY + (TAB_H - fontRendererObj.FONT_HEIGHT) / 2;
            fontRendererObj.drawString(lbl, lx, ly, active ? C_TAB_ACT : C_TAB_IDLE);

            if (active) {
                // 2px purple underline
                RenderUtils.drawRect(tx + 6, tabBarY + TAB_H - 2, tx + tabW - 6, tabBarY + TAB_H, C_TAB_LINE);
            }
        }
    }

    private void drawContent(int mx, int my) {
        switch (activeTab) {
            case TAB_TIMER:   drawTimerTab  (mx, my); break;
            case TAB_ALERTS:  drawAlertsTab (mx, my); break;
            case TAB_DISPLAY: drawDisplayTab(mx, my); break;
        }
    }

    // ── Tab content ───────────────────────────────────────────────────────────

    private void drawTimerTab(int mx, int my) {
        int cs = py + CONT_OFF;

        drawSectionLabel("Timer Settings", cs - 5);
        tsTimerEn.draw(fontRendererObj, mx, my);
        tsShowMs .draw(fontRendererObj, mx, my);
        tsRainbow.draw(fontRendererObj, mx, my);

        int tcY = cs + ROW*3 + SEC_H;
        drawSectionLabel("Timer Color", tcY - 5);
        drawSwatch(pTimer, px + W - PAD - 12, tcY - 5, 10, 10);
        slTimerR.draw(fontRendererObj, mx, my);
        slTimerG.draw(fontRendererObj, mx, my);
        slTimerB.draw(fontRendererObj, mx, my);

        int opY = tcY + ROW*3 + SEC_H;
        drawSectionLabel("HUD Opacity", opY - 5);
        slOpacity.draw(fontRendererObj, mx, my);
    }

    private void drawAlertsTab(int mx, int my) {
        int cs = py + CONT_OFF;

        drawSectionLabel("Detection", cs - 5);
        tsAlertEn   .draw(fontRendererObj, mx, my);
        tsSoundAlert.draw(fontRendererObj, mx, my);
        tsFlash     .draw(fontRendererObj, mx, my);

        int acY = cs + ROW*3 + SEC_H;
        drawSectionLabel("Approaching Color  [!]", acY - 5);
        drawSwatch(pAppr, px + W - PAD - 12, acY - 5, 10, 10);
        slApprR.draw(fontRendererObj, mx, my);
        slApprG.draw(fontRendererObj, mx, my);
        slApprB.draw(fontRendererObj, mx, my);

        int rvY = acY + ROW*3 + SEC_H;
        drawSectionLabel("Arrived Color  [!!]", rvY - 5);
        drawSwatch(pArrv, px + W - PAD - 12, rvY - 5, 10, 10);
        slArrvR.draw(fontRendererObj, mx, my);
        slArrvG.draw(fontRendererObj, mx, my);
        slArrvB.draw(fontRendererObj, mx, my);

        int snY = rvY + ROW*3 + SEC_H;
        drawSectionLabel("Detection Radius", snY - 5);
        slSens.draw(fontRendererObj, mx, my);
    }

    private void drawDisplayTab(int mx, int my) {
        int cs = py + CONT_OFF;
        drawSectionLabel("HUD", cs - 5);
        tsCastCnt.draw(fontRendererObj, mx, my);
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    /**
     * Section label:  "label text ─────────────────────────────────────────"
     * The line extends from the end of the label to the right panel edge.
     */
    private void drawSectionLabel(String label, int y) {
        int lx  = px + PAD;
        int lw  = fontRendererObj.getStringWidth(label);
        int ly  = y + (fontRendererObj.FONT_HEIGHT - 1) / 2;

        fontRendererObj.drawString(label, lx, y, C_SEC_TEXT);
        // Right-side divider
        int lineX = lx + lw + 8;
        if (lineX < px + W - PAD) {
            RenderUtils.drawRect(lineX, ly, px + W - PAD, ly + 1, C_DIVIDER);
        }
    }

    /** Tiny inline color swatch (shows current mixed color). */
    private void drawSwatch(int rgb, int x, int y, int w, int h) {
        RenderUtils.drawRect(x - 1, y - 1, x + w + 1, y + h + 1, C_OUTLINE);
        RenderUtils.drawRect(x, y, x + w, y + h, 0xFF000000 | rgb);
    }

    private void drawFooterBtn(int[] b, String label, int bg, int border, int textColor, int mx, int my) {
        boolean hov = mx >= b[0] && mx < b[0]+b[2] && my >= b[1] && my < b[1]+b[3];
        int fill = hov ? blendA(bg, 0x20) : bg;
        RenderUtils.drawRect(b[0], b[1], b[0]+b[2], b[1]+b[3], fill);
        RenderUtils.drawBorder(b[0], b[1], b[0]+b[2], b[1]+b[3], border);
        int tx = b[0] + (b[2] - fontRendererObj.getStringWidth(label)) / 2;
        int ty = b[1] + (b[3] - fontRendererObj.FONT_HEIGHT) / 2;
        fontRendererObj.drawString(label, tx, ty, textColor);
    }

    private static int blendA(int color, int extraAlpha) {
        int a = Math.min(255, ((color >> 24) & 0xFF) + extraAlpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private void refreshPreviews() {
        pTimer = ColorUtils.fromRGB(slTimerR.getValue(), slTimerG.getValue(), slTimerB.getValue());
        pAppr  = ColorUtils.fromRGB(slApprR.getValue(),  slApprG.getValue(),  slApprB.getValue());
        pArrv  = ColorUtils.fromRGB(slArrvR.getValue(),  slArrvG.getValue(),  slArrvB.getValue());
    }

    // ── Mouse events ──────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        if (btn != 0) return;

        // [×] header close
        if (mx >= px + W - 24 && mx < px + W - 8 && my >= py + 8 && my < py + 24) {
            mc.displayGuiScreen(null); return;
        }

        // Tab switch
        int tabW = W / TAB_LABELS.length;
        int tabY = py + HEADER_H;
        if (my >= tabY && my < tabY + TAB_H) {
            int clicked = (mx - px) / tabW;
            if (clicked >= 0 && clicked < TAB_LABELS.length) {
                activeTab = clicked; return;
            }
        }

        // Content
        routeToggleClick(mx, my);
        routeSliderPress(mx, my);

        // Footer
        if (inBounds(mx, my, bSave))  { applyAndSave();                        mc.displayGuiScreen(null); }
        if (inBounds(mx, my, bReset)) { FishingTracker.INSTANCE.resetStats(); }
        if (inBounds(mx, my, bClose)) { mc.displayGuiScreen(null); }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long timeSince) {
        if (btn != 0) return;
        MEFSlider[] active = activeTabSliders();
        if (active != null) for (MEFSlider s : active) s.mouseDragged(mx);
        refreshPreviews();
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        MEFSlider[] active = activeTabSliders();
        if (active != null) for (MEFSlider s : active) s.mouseReleased();
    }

    // ── Routing helpers ───────────────────────────────────────────────────────

    private void routeToggleClick(int mx, int my) {
        Config cfg = EnoughFishing.INSTANCE.getConfig();
        ToggleSwitch[] switches = activeTabToggles();
        if (switches == null) return;
        for (ToggleSwitch ts : switches) {
            if (!ts.handleClick(mx, my)) continue;
            switch (ts.id) {
                case ID_TIMER_EN: cfg.setTimerEnabled(ts.isEnabled());           break;
                case ID_SHOW_MS:  cfg.setShowMilliseconds(ts.isEnabled());       break;
                case ID_RAINBOW:  cfg.setRainbowMode(ts.isEnabled());            break;
                case ID_ALERT_EN: cfg.setParticleAlertsEnabled(ts.isEnabled());  break;
                case ID_SOUND:    cfg.setSoundAlertEnabled(ts.isEnabled());      break;
                case ID_FLASH:    cfg.setScreenFlashEnabled(ts.isEnabled());     break;
                case ID_CAST_CNT: cfg.setShowCastCount(ts.isEnabled());          break;
            }
            break;
        }
    }

    private void routeSliderPress(int mx, int my) {
        MEFSlider[] sliders = activeTabSliders();
        if (sliders == null) return;
        for (MEFSlider s : sliders) s.mousePressed(mx, my);
    }

    private ToggleSwitch[] activeTabToggles() {
        switch (activeTab) {
            case TAB_TIMER:   return new ToggleSwitch[]{tsTimerEn, tsShowMs, tsRainbow};
            case TAB_ALERTS:  return new ToggleSwitch[]{tsAlertEn, tsSoundAlert, tsFlash};
            case TAB_DISPLAY: return new ToggleSwitch[]{tsCastCnt};
            default: return null;
        }
    }

    private MEFSlider[] activeTabSliders() {
        switch (activeTab) {
            case TAB_TIMER:  return timerSliders;
            case TAB_ALERTS: return alertSliders;
            default: return null;
        }
    }

    private void applyAndSave() {
        Config cfg = EnoughFishing.INSTANCE.getConfig();
        cfg.setTimerColor        (ColorUtils.fromRGB(slTimerR.getValue(), slTimerG.getValue(), slTimerB.getValue()));
        cfg.setHudOpacity        (slOpacity.getValue() / 255f);
        cfg.setApproachingColor  (ColorUtils.fromRGB(slApprR.getValue(),  slApprG.getValue(),  slApprB.getValue()));
        cfg.setArrivedColor      (ColorUtils.fromRGB(slArrvR.getValue(),  slArrvG.getValue(),  slArrvB.getValue()));
        cfg.setParticleSensitivity(slSens.getValue() / 10f);
        cfg.save();
    }

    private static boolean inBounds(int mx, int my, int[] b) {
        return mx >= b[0] && mx < b[0]+b[2] && my >= b[1] && my < b[1]+b[3];
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
