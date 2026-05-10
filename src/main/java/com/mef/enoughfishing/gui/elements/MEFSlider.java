package com.mef.enoughfishing.gui.elements;

import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.gui.FontRenderer;

/**
 * Horizontal drag-slider with a centered value readout.
 *
 * <p>Not a {@link net.minecraft.client.gui.GuiButton} subclass — mouse
 * events are delegated from the parent {@link net.minecraft.client.gui.GuiScreen}
 * manually, which gives us full control over the drag behaviour.</p>
 *
 * <p>No allocations during draw — only primitive arithmetic.</p>
 */
public final class MEFSlider {

    private static final int TRACK_COLOR  = 0xFF444455;
    private static final int FILL_COLOR   = 0xFF2266BB;
    private static final int KNOB_IDLE    = 0xFF9999AA;
    private static final int KNOB_DRAG    = 0xFFCCCCDD;
    private static final int TEXT_COLOR   = 0xFFCCCCCC;

    public final int x, y, width, height;
    private final String label;
    private final int    min, max;

    private int     value;
    private boolean dragging;

    public MEFSlider(int x, int y, int width, int height,
                     String label, int value, int min, int max) {
        this.x      = x;      this.y      = y;
        this.width  = width;  this.height = height;
        this.label  = label;
        this.min    = min;    this.max    = max;
        this.value  = clamp(value);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    public void draw(FontRenderer fr, int mouseX, int mouseY) {
        // Track
        RenderUtils.drawRect(x, y + height / 2 - 1, x + width, y + height / 2 + 1, TRACK_COLOR);

        // Filled portion
        float pct      = pct();
        int   fillEdge = x + (int)(width * pct);
        if (fillEdge > x) {
            RenderUtils.drawRect(x, y + height / 2 - 1, fillEdge, y + height / 2 + 1, FILL_COLOR);
        }

        // Knob
        int knobCx = x + (int)(width * pct);
        RenderUtils.drawRect(knobCx - 3, y, knobCx + 3, y + height, dragging ? KNOB_DRAG : KNOB_IDLE);

        // Label : value  (drawn to the right of the track)
        String text  = label + ": " + value;
        int    textX = x + width + 6;
        fr.drawStringWithShadow(text, textX, y + (height - fr.FONT_HEIGHT) / 2, TEXT_COLOR);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    /** @return true if this slider consumed the click. */
    public boolean mousePressed(int mouseX, int mouseY) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y - 2 && mouseY <= y + height + 2) {
            dragging = true;
            updateFromMouse(mouseX);
            return true;
        }
        return false;
    }

    public void mouseDragged(int mouseX) {
        if (dragging) updateFromMouse(mouseX);
    }

    public void mouseReleased() {
        dragging = false;
    }

    // ── Value access ──────────────────────────────────────────────────────────

    public int  getValue()     { return value; }
    public void setValue(int v){ value = clamp(v); }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void updateFromMouse(int mouseX) {
        float raw = (mouseX - x) / (float) width;
        value = min + Math.round(clamp01(raw) * (max - min));
    }

    private float   pct()          { return (float)(value - min) / (max - min); }
    private int     clamp(int v)   { return Math.max(min, Math.min(max, v)); }
    private float   clamp01(float f){ return Math.max(0f, Math.min(1f, f)); }
}
