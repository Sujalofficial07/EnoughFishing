package com.mef.enoughfishing.events;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.utils.ColorUtils;
import com.mef.enoughfishing.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders the fishing HUD and screen-flash alert.
 *
 * <p><b>GC policy:</b> no objects are created inside the hot path.
 * {@link FishingTracker#getFormattedTime} reuses its internal StringBuilder.
 * Color arithmetic uses only primitives. {@link ScaledResolution} is created
 * at most once per frame and only when the alert is actually visible.</p>
 */
@SideOnly(Side.CLIENT)
public final class RenderEventHandler {

    private static final int  PANEL_PADDING = 4;
    private static final int  PANEL_COLOR   = 0x88000000; // semi-transparent black

    private final Minecraft     mc      = Minecraft.getMinecraft();
    private final FishingTracker tracker = FishingTracker.INSTANCE;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        // ElementType.ALL fires after every other overlay element is drawn.
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;

        Config cfg = EnoughFishing.INSTANCE.getConfig();

        if (cfg.isTimerEnabled() && tracker.isFishing()) {
            renderTimerHUD(cfg);
        }

        if (cfg.isScreenFlashEnabled() && tracker.isAlertActive()) {
            renderScreenFlash(cfg);
        }
    }

    // ── Timer HUD ─────────────────────────────────────────────────────────────

    private void renderTimerHUD(Config cfg) {
        // getFormattedTime() reuses a pre-allocated StringBuilder — zero extra GC.
        String timerStr = tracker.getFormattedTime(cfg.isShowMilliseconds());

        int fontHeight  = mc.fontRendererObj.FONT_HEIGHT;
        int textWidth   = mc.fontRendererObj.getStringWidth(timerStr);
        int x           = cfg.getHudX();
        int y           = cfg.getHudY();
        int alpha       = (int) (cfg.getHudOpacity() * 255f) & 0xFF;
        int lineH       = fontHeight + 3;

        // Determine panel height: one or two lines
        int lines       = cfg.isShowCastCount() ? 2 : 1;
        int castStr     = cfg.isShowCastCount()
                          ? mc.fontRendererObj.getStringWidth("Casts: " + tracker.getCastCount())
                          : 0;
        int panelW      = Math.max(textWidth, castStr) + PANEL_PADDING * 2;
        int panelH      = lineH * lines + PANEL_PADDING * 2;

        // Background panel — no object allocation, just int math
        RenderUtils.drawRect(
            x - PANEL_PADDING,
            y - PANEL_PADDING,
            x - PANEL_PADDING + panelW,
            y - PANEL_PADDING + panelH,
            PANEL_COLOR
        );

        // Timer text
        int timerColor = ColorUtils.withAlpha(cfg.getTimerColor(), alpha);
        mc.fontRendererObj.drawStringWithShadow(timerStr, x, y, timerColor);

        // Cast count (second line)
        if (cfg.isShowCastCount()) {
            int mutedColor = ColorUtils.withAlpha(0xAAAAAA, alpha);
            mc.fontRendererObj.drawStringWithShadow("Casts: " + tracker.getCastCount(), x, y + lineH, mutedColor);
        }
    }

    // ── Screen-flash alert ────────────────────────────────────────────────────

    private void renderScreenFlash(Config cfg) {
        float alertAlpha = tracker.getAlertAlpha();
        if (alertAlpha <= 0f) return;

        int   color   = cfg.getAlertColor();
        int   r       = ColorUtils.getRed(color);
        int   g       = ColorUtils.getGreen(color);
        int   b       = ColorUtils.getBlue(color);
        int   a       = (int) (alertAlpha * 70f);          // max 70/255 ≈ 27 % opacity

        // ScaledResolution only created when alert is visible — acceptable allocation.
        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtils.drawFullscreenRect(sr.getScaledWidth(), sr.getScaledHeight(), r, g, b, a);
    }
}
