package com.mef.enoughfishing.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Thin GL rendering utilities.
 *
 * <p><b>GC policy:</b> no heap allocations. {@link Tessellator} is a singleton;
 * {@link WorldRenderer} is obtained from it. All color components are
 * computed inline from packed ints using bit-shifts — zero boxing.</p>
 *
 * <p>All methods leave GL state equivalent to what they found it in
 * (blend disabled, texture enabled, color reset to white).</p>
 */
public final class RenderUtils {

    private RenderUtils() {}

    // ── Filled rectangle ──────────────────────────────────────────────────────

    /**
     * Draws a solid ARGB rectangle.
     * @param color packed ARGB (high byte = alpha)
     */
    public static void drawRect(double x1, double y1, double x2, double y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ( color        & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, a);

        Tessellator    tess = Tessellator.getInstance();
        WorldRenderer  wr   = tess.getWorldRenderer();
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

    // ── Fullscreen overlay ────────────────────────────────────────────────────

    /**
     * Covers the entire 2-D HUD canvas with a translucent color.
     * Intended for the screen-flash alert.
     */
    public static void drawFullscreenRect(int screenW, int screenH, int r, int g, int b, int a) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);

        Tessellator   tess = Tessellator.getInstance();
        WorldRenderer wr   = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(0,       screenH, 0).endVertex();
        wr.pos(screenW, screenH, 0).endVertex();
        wr.pos(screenW, 0,       0).endVertex();
        wr.pos(0,       0,       0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
