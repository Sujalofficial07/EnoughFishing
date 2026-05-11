package com.mef.enoughfishing.events;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.core.FishingTracker.AlertState;
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
 * Floating label that appears above the fishing bobber in 3D world-space.
 *
 * Layout (local units after billboard transform & scale):
 *
 *   APPROACHING only:    [  !  ]     ← yellow, gently pulsing
 *                        [3.24s]
 *
 *   ARRIVED:             [ !!  ]     ← red, faster pulse
 *                        [3.24s]
 *
 *   Fishing, no alert:   [3.24s]
 *
 * No flickering: the label text and color are driven entirely by
 * FishingTracker.getAlertState() which holds each state for a minimum
 * duration even if particle spawns are sparse.
 *
 * The sinusoidal alpha pulse uses System.currentTimeMillis() so it
 * continues smoothly through server lag without stutter.
 */
@SideOnly(Side.CLIENT)
public final class RenderWorldHandler {

    // ── Visual constants ──────────────────────────────────────────────────────
    private static final float  SCALE      = 0.025f;
    private static final float  LIFT       = 0.55f;   // blocks above bobber anchor
    private static final int    BG_COLOR   = 0xCC04020D;

    private final Minecraft      mc      = Minecraft.getMinecraft();
    private final FishingTracker tracker = FishingTracker.INSTANCE;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Config cfg = EnoughFishing.INSTANCE.getConfig();
        if (!cfg.isTimerEnabled() || !tracker.isFishing()) return;
        if (mc.thePlayer == null) return;

        EntityFishHook hook = mc.thePlayer.fishEntity;
        if (hook == null) return;

        renderLabel(hook, event.partialTicks, cfg);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private void renderLabel(EntityFishHook hook, float pt, Config cfg) {
        Entity cam = mc.getRenderViewEntity();
        double cx = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * pt;
        double cy = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * pt;
        double cz = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * pt;

        double rx = hook.lastTickPosX + (hook.posX - hook.lastTickPosX) * pt - cx;
        double ry = hook.lastTickPosY + (hook.posY - hook.lastTickPosY) * pt - cy + LIFT;
        double rz = hook.lastTickPosZ + (hook.posZ - hook.lastTickPosZ) * pt - cz;

        AlertState state    = tracker.getAlertState();
        String timerText    = tracker.getFormattedTime(cfg.isShowMilliseconds());
        String alertText    = alertIndicator(state);   // "!!", "!", or null
        int    alertRGB     = alertRGB(state, cfg);
        float  alertAlpha   = tracker.getAlertPulse();
        int    timerColor   = timerColor(state, cfg);
        int    borderColor  = borderColor(state, cfg);

        int timerW = mc.fontRendererObj.getStringWidth(timerText);
        int alertW = alertText != null ? mc.fontRendererObj.getStringWidth(alertText) : 0;
        int maxW   = Math.max(timerW, alertW);
        int fh     = mc.fontRendererObj.FONT_HEIGHT;

        int hw     = maxW / 2 + 5;
        int topY   = (alertText != null) ? -(fh + 3) : -2;

        // ── GL setup ──────────────────────────────────────────────────────────
        GlStateManager.pushMatrix();
        GlStateManager.translate(rx, ry, rz);
        GL11.glRotatef(-mc.getRenderManager().playerViewY,  0f, 1f, 0f);
        GL11.glRotatef( mc.getRenderManager().playerViewX,  1f, 0f, 0f);
        GlStateManager.scale(-SCALE, -SCALE, SCALE);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // ── Background + coloured border ──────────────────────────────────────
        // Outer border glow (soft halo)
        RenderUtils.drawRect(-hw - 2, topY - 2, hw + 2, fh + 4, borderColor & 0x55FFFFFF);
        // Solid 1px border
        RenderUtils.drawRect(-hw - 1, topY - 1, hw + 1, fh + 3, borderColor);
        // Dark background
        RenderUtils.drawRect(-hw, topY, hw, fh + 2, BG_COLOR);

        // ── Alert indicator ───────────────────────────────────────────────────
        if (alertText != null) {
            int packedAlertColor = ColorUtils.withAlpha(alertRGB, (int)(alertAlpha * 255f));
            mc.fontRendererObj.drawStringWithShadow(
                alertText, -(alertW / 2f), -(fh + 1f), packedAlertColor);
        }

        // ── Timer ─────────────────────────────────────────────────────────────
        mc.fontRendererObj.drawStringWithShadow(timerText, -(timerW / 2f), 0f, timerColor);

        // ── GL restore ────────────────────────────────────────────────────────
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns "!!" for ARRIVED, "!" for APPROACHING, null for NONE. */
    private static String alertIndicator(AlertState state) {
        switch (state) {
            case ARRIVED:     return "!!";
            case APPROACHING: return "!";
            default:          return null;
        }
    }

    /** Packed RGB (no alpha) for the alert indicator text. */
    private static int alertRGB(AlertState state, Config cfg) {
        switch (state) {
            case ARRIVED:     return cfg.getArrivedColor()     & 0x00FFFFFF;
            case APPROACHING: return cfg.getApproachingColor() & 0x00FFFFFF;
            default:          return 0xFFFFFF;
        }
    }

    /** Packed ARGB for the label border. Matches the current alert colour. */
    private static int borderColor(AlertState state, Config cfg) {
        int base;
        switch (state) {
            case ARRIVED:     base = cfg.getArrivedColor();     break;
            case APPROACHING: base = cfg.getApproachingColor(); break;
            default:          base = 0x7722BB; break;
        }
        return 0xBB000000 | (base & 0x00FFFFFF);
    }

    /**
     * Timer text color.
     * • Rainbow mode → full hue cycle
     * • Alert active → blend toward the arrived color for visual cohesion
     * • Otherwise    → configured color (fully opaque)
     */
    private static int timerColor(AlertState state, Config cfg) {
        if (cfg.isRainbowMode()) {
            return 0xFF000000 | ColorUtils.rainbowRGB(System.currentTimeMillis(), 0);
        }
        if (state == AlertState.ARRIVED) {
            long elapsed = System.currentTimeMillis() % 600L;
            float t = (elapsed < 300L) ? elapsed / 300f : 1f - (elapsed - 300f) / 300f;
            return 0xFF000000 | ColorUtils.blendRGB(cfg.getTimerColor(), cfg.getArrivedColor(), t * 0.5f);
        }
        return 0xFF000000 | cfg.getTimerColor();
    }
}
