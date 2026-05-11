package com.mef.enoughfishing.gui.elements;

import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.gui.FontRenderer;

/**
 * Lunar-style horizontal drag slider.
 *
 * Row layout (rowWidth pixels wide):
 *  [LABEL_W] 4px [──────track──────] 4px [VALUE_W]
 *
 * Track is 3px tall; knob is a 12px circle centred on the track.
 * All positions are determined at construction — no per-frame allocation.
 */
public final class MEFSlider {

    // ── Dimensions ────────────────────────────────────────────────────────────
    public static final int ROW_H   = 28;
    private static final int LABEL_W = 36;
    private static final int VALUE_W = 30;
    private static final int TRACK_H = 3;
    private static final int KNOB_D  = 12;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_LABEL   = 0xFF7474A0;
    private static final int C_VALUE   = 0xFFCCCCEE;
    private static final int C_TRACK   = 0xFF1A1B2C;
    private static final int C_FILL    = 0xFF604CE0;  // purple fill
    private static final int C_KNOB    = 0xFFE0E0F8;  // bright knob
    private static final int C_KNOB_DG = 0xFF8888B0;  // knob while dragging

    // ── Fields ────────────────────────────────────────────────────────────────
    public final int    x, y, rowWidth;
    private final String label;
    private final int   min, max;
    private int         value;
    private boolean     dragging;

    // Pre-computed track geometry (avoids recalculating every frame)
    private final int trackX, trackW, trackY;

    public MEFSlider(int x, int y, int rowWidth, String label, int value, int min, int max) {
        this.x = x;  this.y = y;  this.rowWidth = rowWidth;
        this.label = label;
        this.min = min;  this.max = max;
        this.value = clamp(value);

        this.trackX = x + LABEL_W + 4;
        this.trackW = rowWidth - LABEL_W - VALUE_W - 8;
        this.trackY = y + (ROW_H - TRACK_H) / 2;
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(FontRenderer fr, int mx, int my) {
        int cy = y + ROW_H / 2;  // row vertical centre

        // Label
        fr.drawString(label, x, cy - fr.FONT_HEIGHT / 2, C_LABEL);

        // Track background
        RenderUtils.drawRect(trackX, trackY, trackX + trackW, trackY + TRACK_H, C_TRACK);

        // Fill (from left edge to knob position)
        float pct     = pct();
        int   fillEnd = trackX + (int)(trackW * pct);
        if (fillEnd > trackX) {
            RenderUtils.drawRect(trackX, trackY, fillEnd, trackY + TRACK_H, C_FILL);
        }

        // Knob — circle centred on the fill/track junction
        int kx = trackX + (int)(trackW * pct) - KNOB_D / 2;
        int ky = cy - KNOB_D / 2;
        RenderUtils.drawCircle(kx, ky, KNOB_D, dragging ? C_KNOB_DG : C_KNOB);

        // Value (right-aligned in the value column)
        String vs   = String.valueOf(value);
        int    valX = x + rowWidth - fr.getStringWidth(vs);
        fr.drawString(vs, valX, cy - fr.FONT_HEIGHT / 2, C_VALUE);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    /** @return true if this slider consumed the press. */
    public boolean mousePressed(int mx, int my) {
        if (mx >= trackX - 6 && mx <= trackX + trackW + 6
                && my >= y && my <= y + ROW_H) {
            dragging = true;
            updateValue(mx);
            return true;
        }
        return false;
    }

    public void mouseDragged(int mx) { if (dragging) updateValue(mx); }
    public void mouseReleased()      { dragging = false; }

    // ── Value ─────────────────────────────────────────────────────────────────

    public int  getValue()      { return value; }
    public void setValue(int v) { value = clamp(v); }

    public static int getRowHeight() { return ROW_H; }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void updateValue(int mx) {
        float pct = clamp01((float)(mx - trackX) / trackW);
        value = min + Math.round(pct * (max - min));
    }

    private float pct()            { return (float)(value - min) / (max - min); }
    private int   clamp(int v)     { return Math.max(min, Math.min(max, v)); }
    private float clamp01(float f) { return Math.max(0f, Math.min(1f, f)); }
}
