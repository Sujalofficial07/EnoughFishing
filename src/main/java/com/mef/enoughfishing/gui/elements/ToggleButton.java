package com.mef.enoughfishing.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * A two-state toggle that extends {@link GuiButton} so it participates in
 * the normal {@code buttonList} / {@code actionPerformed} flow.
 * State is indicated by color-coded checkmarks without any texture lookups.
 */
public final class ToggleButton extends GuiButton {

    private final String label;
    private boolean      enabled;

    public ToggleButton(int id, int x, int y, int width, int height,
                        String label, boolean initialState) {
        super(id, x, y, width, height, "");
        this.label   = label;
        this.enabled = initialState;
        refreshText();
    }

    /** Flips the toggle state and updates the display string. */
    public void toggle() {
        enabled = !enabled;
        refreshText();
    }

    public boolean isEnabled()       { return enabled; }
    public void    setEnabled(boolean v) { enabled = v; refreshText(); }

    private void refreshText() {
        displayString = (enabled ? "§a✔ " : "§7✘ ") + label;
    }

    // Override to tint the button background when active
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        // Tint: green-ish when enabled, default gray otherwise
        if (enabled) {
            // Draw a subtle colored background before the default chrome
            net.minecraft.client.renderer.GlStateManager.color(0.5f, 0.9f, 0.5f, 0.15f);
        }
        super.drawButton(mc, mouseX, mouseY);
        net.minecraft.client.renderer.GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
