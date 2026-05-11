package com.mef.enoughfishing.gui.elements;

import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Neon-styled toggle button.
 * Enabled  → neon-green border + tinted green background, bright text.
 * Disabled → dim purple border + dark background, muted text.
 */
public final class ToggleButton extends GuiButton {

    // Enabled palette
    private static final int EN_BG     = 0xCC00280F;
    private static final int EN_BORDER = 0xFF00FF88;
    private static final int EN_TEXT   = 0xFF00FF88;
    private static final int EN_HOVER  = 0x2200FF88;

    // Disabled palette
    private static final int DIS_BG     = 0xCC0A0018;
    private static final int DIS_BORDER = 0xFF5B246E;
    private static final int DIS_TEXT   = 0xFF7744AA;
    private static final int DIS_HOVER  = 0x229D4EDD;

    private final String label;
    private boolean      enabled;

    public ToggleButton(int id, int x, int y, int w, int h, String label, boolean init) {
        super(id, x, y, w, h, "");
        this.label   = label;
        this.enabled = init;
        refreshText();
    }

    // ── State ─────────────────────────────────────────────────────────────────

    public void    toggle()           { enabled = !enabled; refreshText(); }
    public boolean isEnabled()        { return enabled; }
    public void    setEnabled(boolean v){ enabled = v; refreshText(); }

    private void refreshText() {
        // Color codes stripped in drawButton; kept for fallback rendering only
        displayString = (enabled ? "§a✔ " : "§8✘ ") + label;
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    @Override
    public void drawButton(Minecraft mc, int mx, int my) {
        if (!visible) return;

        boolean hovered = mx >= xPosition && my >= yPosition
                       && mx < xPosition + width && my < yPosition + height;

        int bg     = enabled ? EN_BG     : DIS_BG;
        int border = enabled ? EN_BORDER : DIS_BORDER;
        int text   = enabled ? EN_TEXT   : DIS_TEXT;
        int hover  = enabled ? EN_HOVER  : DIS_HOVER;

        // Fill
        RenderUtils.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bg);
        // Hover overlay
        if (hovered) RenderUtils.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, hover);
        // Border (1-pixel outline)
        RenderUtils.drawBorder(xPosition, yPosition, xPosition + width, yPosition + height, border);

        // Label — draw without § codes
        String plain = (enabled ? "\u2714 " : "\u2718 ") + label;
        int tx = xPosition + width / 2 - mc.fontRendererObj.getStringWidth(plain) / 2;
        int ty = yPosition + (height - mc.fontRendererObj.FONT_HEIGHT) / 2;
        mc.fontRendererObj.drawStringWithShadow(plain, tx, ty, text);
    }
}
