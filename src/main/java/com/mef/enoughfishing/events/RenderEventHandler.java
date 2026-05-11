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

@SideOnly(Side.CLIENT)
public final class RenderEventHandler {

    private final Minecraft      mc      = Minecraft.getMinecraft();
    private final FishingTracker tracker = FishingTracker.INSTANCE;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;

        Config cfg = EnoughFishing.INSTANCE.getConfig();

        // Screen flash — only triggered by ARRIVED (inner-zone) detection
        if (cfg.isScreenFlashEnabled() && tracker.isFlashActive()) {
            renderFlash(cfg);
        }
    }

    private void renderFlash(Config cfg) {
        float alpha = tracker.getFlashAlpha();
        if (alpha <= 0f) return;

        int   color = cfg.getArrivedColor();  // flash in arrived colour
        int   r     = ColorUtils.getRed(color);
        int   g     = ColorUtils.getGreen(color);
        int   b     = ColorUtils.getBlue(color);
        int   a     = (int)(alpha * 55f);     // max ~22% opacity — visible but not blinding

        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtils.drawFullscreenRect(sr.getScaledWidth(), sr.getScaledHeight(), r, g, b, a);
    }
}
