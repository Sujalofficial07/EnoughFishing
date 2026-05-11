package com.mef.enoughfishing.events;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.utils.ColorUtils;
import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Renders a floating timer tag directly above the fishing bobber in 3D world-space.
 *
 * <p>Uses RenderWorldLastEvent so the timer floats in the world exactly like NEU's
 * entity labels. Billboard rotation keeps the text facing the camera at all times.
 * Depth-test is disabled so the tag is always visible through water.</p>
 *
 * <p>Color modes:
 * <ul>
 *   <li>Rainbow — cycles through the full hue spectrum using system time.</li>
 *   <li>Alert pulse — blends configured color → neon green when bite is detected.</li>
 *   <li>Static — the configured timer color.</li>
 * </ul></p>
 */
@SideOnly(Side.CLIENT)
public final class RenderWorldHandler {

    // ── Visual constants ──────────────────────────────────────────────────────
    private static final float SCALE       = 0.026f;  // world-to-screen scale for labels
    private static final float BOBBER_LIFT = 0.55f;   // blocks above bobber anchor
    private static final int   COLOR_BG    = 0xCC05020F; // near-black background
    private static final int   COLOR_BORDER_IDLE  = 0xFF7722BB; // purple
    private static final int   COLOR_BORDER_ALERT = 0xFF00FF88; // neon green on bite

    private final Minecraft      mc      = Minecraft.getMinecraft();
    private final FishingTracker tracker = FishingTracker.INSTANCE;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Config cfg = EnoughFishing.INSTANCE.getConfig();
        if (!cfg.isTimerEnabled() || !tracker.isFishing()) return;

        if (mc.thePlayer == null) return;
        EntityFishHook hook = mc.thePlayer.fishEntity;
        if (hook == null) return;

        drawBobberLabel(hook, event.partialTicks, cfg);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void drawBobberLabel(EntityFishHook hook, float pt, Config cfg) {
        // Interpolated camera position
        Entity cam = mc.getRenderViewEntity();
        double cx = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * pt;
        double cy = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * pt;
        double cz = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * pt;

        // Interpolated bobber position, relative to camera, lifted above water
        double rx = hook.lastTickPosX + (hook.posX - hook.lastTickPosX) * pt - cx;
        double ry = hook.lastTickPosY + (hook.posY - hook.lastTickPosY) * pt - cy + BOBBER_LIFT;
        double rz = hook.lastTickPosZ + (hook.posZ - hook.lastTickPosZ) * pt - cz;

        // Build timer string (reuses FishingTracker's pre-allocated StringBuilder)
        String text  = buildTimerLabel(cfg);
        int    textW = mc.fontRendererObj.getStringWidth(text);
        int    fh    = mc.fontRendererObj.FONT_HEIGHT;
        int    textColor = getTimerColor(cfg);
        int    borderColor = tracker.isAlertActive() ? COLOR_BORDER_ALERT : COLOR_BORDER_IDLE;

        // ── GL setup ──────────────────────────────────────────────────────────
        GlStateManager.pushMatrix();
        GlStateManager.translate(rx, ry, rz);

        // Billboard: rotate to always face the camera
        GL11.glRotatef(-mc.getRenderManager().playerViewY,  0f, 1f, 0f);
        GL11.glRotatef( mc.getRenderManager().playerViewX,  1f, 0f, 0f);

        // Flip Y (MC's font renders Y-down; world-space Y is Y-up)
        GlStateManager.scale(-SCALE, -SCALE, SCALE);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // ── Background + border ───────────────────────────────────────────────
        int hw = textW / 2 + 5;  // half-width of tag

        // Outer border glow (1px halo)
        RenderUtils.drawRect(-hw - 1, -3, hw + 1, fh + 4, borderColor & 0x88FFFFFF);
        // Solid border
        RenderUtils.drawRect(-hw,     -2, hw,     fh + 3, borderColor);
        // Dark background
        RenderUtils.drawRect(-hw + 1, -1, hw - 1, fh + 2, COLOR_BG);

        // ── Timer text ────────────────────────────────────────────────────────
        mc.fontRendererObj.drawStringWithShadow(text, -(textW / 2f), 0f, textColor);

        // ── GL restore ────────────────────────────────────────────────────────
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the display string. Adds decorative bookends that pulse on alert.
     * Example: "◈ 3.247s ◈" or "3.247s"
     */
    private String buildTimerLabel(Config cfg) {
        String core = tracker.getFormattedTime(cfg.isShowMilliseconds());
        if (tracker.isAlertActive()) {
            return "◈ " + core + " ◈";
        }
        return core;
    }

    /**
     * Returns the packed ARGB color for the timer text.
     *
     * Priority order:
     * 1. Alert active  → pulse between configured color and neon green
     * 2. Rainbow mode  → full hue cycle keyed to system time
     * 3. Static        → configured color
     */
    private int getTimerColor(Config cfg) {
        if (tracker.isAlertActive()) {
            float t = tracker.getAlertAlpha();
            // Pulse: config color <-> bright neon green
            int pulsed = ColorUtils.blendRGB(cfg.getTimerColor(), 0x00FF88, t);
            return 0xFF000000 | pulsed;
        }
        if (cfg.isRainbowMode()) {
            return 0xFF000000 | ColorUtils.rainbowRGB(System.currentTimeMillis(), 0);
        }
        return 0xFF000000 | cfg.getTimerColor();
    }
}
