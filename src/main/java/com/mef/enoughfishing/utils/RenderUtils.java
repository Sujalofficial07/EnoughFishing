package com.mef.enoughfishing.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Zero-allocation GL drawing utilities.
 * Every method restores GL state to: blend off, texture on, color white.
 */
public final class RenderUtils {

    private RenderUtils() {}

    // ── Filled rect ───────────────────────────────────────────────────────────

    public static void drawRect(double x1, double y1, double x2, double y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ( color        & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, a);

        Tessellator   tess = Tessellator.getInstance();
        WorldRenderer wr   = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x1, y2, 0).endVertex();
        wr.pos(x2, y2, 0).endVertex();
        wr.pos(x2, y1, 0).endVertex();
        wr.pos(x1, y1, 0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    // ── 1-pixel outline ───────────────────────────────────────────────────────

    public static void drawBorder(double x, double y, double x2, double y2, int color) {
        drawRect(x,      y,      x2,     y  + 1, color);
        drawRect(x,      y2 - 1, x2,     y2,     color);
        drawRect(x,      y,      x  + 1, y2,     color);
        drawRect(x2 - 1, y,      x2,     y2,     color);
    }

    // ── Pill (stadium) shape — for toggle switches ────────────────────────────

    /**
     * Draws a filled pill / stadium shape using three overlapping rects that
     * produce 2-pixel corner cuts, giving a clean rounded appearance at
     * the small sizes used by toggle switches (height ≤ 20px).
     */
    public static void drawPill(double x, double y, double w, double h, int color) {
        drawRect(x + 2, y,     x + w - 2, y + h,     color);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, color);
        drawRect(x,     y + 2, x + w,     y + h - 2,  color);
    }

    /**
     * Draws just the 1px border of a pill outline.
     * Used to add a slightly darker edge to toggle knobs and pill backgrounds.
     */
    public static void drawPillBorder(double x, double y, double w, double h, int color) {
        // Top & bottom edges (inset 2px from ends)
        drawRect(x + 2, y,         x + w - 2, y + 1,         color);
        drawRect(x + 2, y + h - 1, x + w - 2, y + h,         color);
        // Left & right edges (inset 2px from top/bottom)
        drawRect(x,         y + 2, x + 1,         y + h - 2, color);
        drawRect(x + w - 1, y + 2, x + w,         y + h - 2, color);
        // Corner pixels
        drawRect(x + 1, y + 1,         x + 2,     y + 2,         color);
        drawRect(x + w - 2, y + 1,     x + w - 1, y + 2,         color);
        drawRect(x + 1, y + h - 2,     x + 2,     y + h - 1,     color);
        drawRect(x + w - 2, y + h - 2, x + w - 1, y + h - 1,     color);
    }

    // ── Approximate circle (for slider knobs / toggle knobs) ──────────────────

    /**
     * Draws a filled approximate circle of diameter {@code d} with top-left at
     * (x, y). Uses two overlapping rects (horizontal + vertical) to produce an
     * octagon that reads as a circle at the sizes used in this mod (8-14px).
     */
    public static void drawCircle(double x, double y, double d, int color) {
        double cx = x + d / 2.0;
        double cy = y + d / 2.0;
        double r  = d / 2.0;
        // Horizontal band (full width, 70% height)
        drawRect(cx - r, cy - r * 0.7, cx + r, cy + r * 0.7, color);
        // Vertical band (70% width, full height)
        drawRect(cx - r * 0.7, cy - r, cx + r * 0.7, cy + r, color);
    }

    // ── Fullscreen overlay ────────────────────────────────────────────────────

    public static void drawFullscreenRect(int w, int h, int r, int g, int b, int a) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);

        Tessellator   tess = Tessellator.getInstance();
        WorldRenderer wr   = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(0, h, 0).endVertex();
        wr.pos(w, h, 0).endVertex();
        wr.pos(w, 0, 0).endVertex();
        wr.pos(0, 0, 0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
