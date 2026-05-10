package com.mef.enoughfishing.events;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.utils.ParticleDetector;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Drives {@link FishingTracker#tick()} and manages the Netty pipeline injection
 * that enables low-level particle packet interception.
 *
 * <p>Priority is NORMAL so vanilla and Optifine event handlers run before us,
 * keeping compatibility with other Skyblock mods.</p>
 */
@SideOnly(Side.CLIENT)
public final class FishingEventHandler {

    /** Name key for our Netty channel handler — must be globally unique. */
    private static final String HANDLER_KEY = "mef_particle_detector";

    private final FishingTracker tracker = FishingTracker.INSTANCE;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        tracker.tick();
        ensureHandlerInjected(mc);
    }

    /**
     * Injects {@link ParticleDetector} into the Netty pipeline once per
     * server connection. Re-injects automatically after server switches
     * (e.g. lobby → Skyblock island) because we always check by key, not flag.
     */
    private void ensureHandlerInjected(Minecraft mc) {
        try {
            if (mc.thePlayer.sendQueue == null) return;
            NetworkManager  netManager = mc.thePlayer.sendQueue.getNetworkManager();
            ChannelPipeline pipeline   = netManager.channel().pipeline();

            if (pipeline.get(HANDLER_KEY) == null) {
                pipeline.addBefore("packet_handler", HANDLER_KEY, new ParticleDetector());
                EnoughFishing.LOG.info("[MEF] ParticleDetector injected into Netty pipeline.");
            }
        } catch (Exception ignored) {
            // Connection not yet ready or context changed — silently retry next tick.
        }
    }
}
