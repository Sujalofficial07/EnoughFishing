package com.mef.enoughfishing.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Stateless GL drawing utilities. Zero heap allocations. All methods restore
 * GL state to "blend off, texture on, color white" on exit.
 */
public final class RenderUtils {

    private RenderUtils() {}

    // ── Core rect ─────────────────────────────────────────────────────────────

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

    // ── Neon glow border ──────────────────────────────────────────────────────

    /**
     * Draws a multi-layer glow border around a rectangle to simulate bloom.
     *
     * @param glowColor packed RGB color (no alpha — alpha is computed per layer)
     */
    public static void drawGlowBorder(int x, int y, int x2, int y2, int glowColor) {
        int rgb = glowColor & 0x00FFFFFF;
        // Layer 3 — outermost, very faint
        drawRect(x - 3, y - 3, x2 + 3, y2 + 3, 0x18000000 | rgb);
        // Layer 2 — mid glow
        drawRect(x - 2, y - 2, x2 + 2, y2 + 2, 0x35000000 | rgb);
        // Layer 1 — crisp inner border
        drawRect(x - 1, y - 1, x2 + 1, y2 + 1, 0x70000000 | rgb);
        // Solid 1px border
        drawRect(x,     y,     x2,     y2,     0xFF000000 | rgb);
    }

    /**
     * Draws only the 1-pixel outline of a rectangle (4 edge rects).
     * Cheaper than drawGlowBorder when bloom is not needed.
     */
    public static void drawBorder(int x, int y, int x2, int y2, int color) {
        drawRect(x,      y,      x2,     y  + 1, color); // top
        drawRect(x,      y2 - 1, x2,     y2,     color); // bottom
        drawRect(x,      y,      x  + 1, y2,     color); // left
        drawRect(x2 - 1, y,      x2,     y2,     color); // right
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
