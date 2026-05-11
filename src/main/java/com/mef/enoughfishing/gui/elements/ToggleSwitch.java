package com.mef.enoughfishing.gui.elements;

import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.gui.FontRenderer;

/**
 * A self-contained toggle switch rendered as a label + pill widget.
 * Not a GuiButton subclass — mouse events are forwarded from GuiScreen
 * via {@link #handleClick(int, int)}.
 *
 * Visual:
 *   OFF  →  Label                [●·····]   (dark pill, dim knob)
 *   ON   →  Label                [·····●]   (green-tinted pill, bright knob)
 */
public final class ToggleSwitch {

    // ── Dimensions ────────────────────────────────────────────────────────────
    public static final int ROW_H  = 28;  // total row height in pixels
    private static final int PILL_W = 36;
    private static final int PILL_H = 16;
    private static final int KNOB_D = PILL_H - 4;  // 12px

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_LABEL   = 0xFFCCCCEE;
    private static final int C_HOVER   = 0x0AFFFFFF;

    private static final int C_ON_BG   = 0xFF143B23;  // dark green background
    private static final int C_ON_BD   = 0xFF005230;  // green border
    private static final int C_ON_KN   = 0xFF00D87E;  // bright green knob

    private static final int C_OFF_BG  = 0xFF1C1D30;  // dark purple-gray bg
    private static final int C_OFF_BD  = 0xFF2A2C45;  // dim border
    private static final int C_OFF_KN  = 0xFF454560;  // dim knob

    // ── Fields ────────────────────────────────────────────────────────────────
    public final int    id;
    public final int    x, y, rowWidth;
    private final String label;
    private boolean     enabled;

    public ToggleSwitch(int id, int x, int y, int rowWidth, String label, boolean init) {
        this.id       = id;
        this.x        = x;  this.y        = y;
        this.rowWidth = rowWidth;
        this.label    = label;
        this.enabled  = init;
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(FontRenderer fr, int mx, int my) {
        boolean hovered = mx >= x && mx < x + rowWidth && my >= y && my < y + ROW_H;

        // Row hover highlight
        if (hovered) RenderUtils.drawRect(x, y, x + rowWidth, y + ROW_H, C_HOVER);

        // Label (vertically centered)
        fr.drawString(label, x + 6, y + (ROW_H - fr.FONT_HEIGHT) / 2, C_LABEL);

        // Pill
        int pillX = x + rowWidth - PILL_W - 6;
        int pillY = y + (ROW_H - PILL_H) / 2;

        int pillBg = enabled ? C_ON_BG : C_OFF_BG;
        int pillBd = enabled ? C_ON_BD : C_OFF_BD;
        int knobC  = enabled ? C_ON_KN : C_OFF_KN;

        RenderUtils.drawPill      (pillX,     pillY,     PILL_W, PILL_H, pillBg);
        RenderUtils.drawPillBorder(pillX,     pillY,     PILL_W, PILL_H, pillBd);

        // Knob: left side when OFF, right side when ON
        int knobX = enabled ? pillX + PILL_W - KNOB_D - 2 : pillX + 2;
        int knobY = pillY + 2;
        RenderUtils.drawCircle(knobX, knobY, KNOB_D, knobC);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    /**
     * @return true if the click fell inside this row and the state was toggled.
     */
    public boolean handleClick(int mx, int my) {
        if (mx >= x && mx < x + rowWidth && my >= y && my < y + ROW_H) {
            enabled = !enabled;
            return true;
        }
        return false;
    }

    public boolean isEnabled()         { return enabled; }
    public void    setEnabled(boolean v){ enabled = v; }
    public static  int getRowHeight()  { return ROW_H; }
}
