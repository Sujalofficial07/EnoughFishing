package com.mef.enoughfishing.gui.elements;

import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.gui.FontRenderer;

/**
 * Horizontal drag-slider with neon green/purple theme.
 * No allocations during draw — purely primitive arithmetic.
 */
public final class MEFSlider {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int TRACK_BG    = 0xFF0A0018;
    private static final int TRACK_FILL  = 0xFF00BB66;   // neon green fill
    private static final int TRACK_EDGE  = 0xFF3D1270;   // purple track border
    private static final int KNOB_IDLE   = 0xFF9D4EDD;   // purple knob
    private static final int KNOB_DRAG   = 0xFF00FF88;   // neon green when dragging
    private static final int TEXT_COL    = 0xFFCCFFE8;   // soft mint label

    public final int x, y, width, height;
    private final String label;
    private final int    min, max;

    private int     value;
    private boolean dragging;

    public MEFSlider(int x, int y, int width, int height,
                     String label, int value, int min, int max) {
        this.x = x;  this.y = y;
        this.width = width;  this.height = height;
        this.label = label;
        this.min = min;  this.max = max;
        this.value = clamp(value);
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(FontRenderer fr, int mouseX, int mouseY) {
        int trackY1 = y + height / 2 - 2;
        int trackY2 = y + height / 2 + 2;

        // Track background + edge border
        RenderUtils.drawRect(x,     trackY1, x + width, trackY2, TRACK_BG);
        RenderUtils.drawBorder(x,   trackY1, x + width, trackY2, TRACK_EDGE);

        // Filled portion
        float pct      = pct();
        int   fillEdge = x + (int)(width * pct);
        if (fillEdge > x + 1) {
            RenderUtils.drawRect(x + 1, trackY1 + 1, fillEdge, trackY2 - 1, TRACK_FILL);
        }

        // Knob — taller than the track for a pill feel
        int kx = x + (int)(width * pct);
        int kColor = dragging ? KNOB_DRAG : KNOB_IDLE;
        RenderUtils.drawRect(kx - 4, y,              kx + 4, y + height,     kColor & 0x66FFFFFF);
        RenderUtils.drawRect(kx - 3, y + 1,          kx + 3, y + height - 1, kColor);

        // Label + value to the right
        String display = label + ": " + value;
        fr.drawStringWithShadow(display, x + width + 7, y + (height - fr.FONT_HEIGHT) / 2, TEXT_COL);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    public boolean mousePressed(int mx, int my) {
        if (mx >= x && mx <= x + width && my >= y - 2 && my <= y + height + 2) {
            dragging = true;
            updateFromMouse(mx);
            return true;
        }
        return false;
    }

    public void mouseDragged(int mx) { if (dragging) updateFromMouse(mx); }
    public void mouseReleased()      { dragging = false; }

    // ── Value ─────────────────────────────────────────────────────────────────

    public int  getValue()     { return value; }
    public void setValue(int v){ value = clamp(v); }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void updateFromMouse(int mx) {
        float raw = (mx - x) / (float) width;
        value = min + Math.round(clamp01(raw) * (max - min));
    }

    private float pct()            { return (float)(value - min) / (max - min); }
    private int   clamp(int v)     { return Math.max(min, Math.min(max, v)); }
    private float clamp01(float f) { return Math.max(0f, Math.min(1f, f)); }
}
