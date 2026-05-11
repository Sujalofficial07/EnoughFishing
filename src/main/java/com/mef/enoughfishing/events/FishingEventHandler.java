package com.mef.enoughfishing.events;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.utils.GuiScheduler;
import com.mef.enoughfishing.utils.ParticleDetector;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FishingEventHandler {

    private static final String HANDLER_KEY = "mef_particle_detector";
    private final FishingTracker tracker = FishingTracker.INSTANCE;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Tick the fishing state machine
        tracker.tick();

        // Tick the GUI scheduler — opens GuiEnoughFishing after chat closes
        GuiScheduler.tick();

        // Ensure our Netty packet interceptor is present
        ensureHandlerInjected(mc);
    }

    private void ensureHandlerInjected(Minecraft mc) {
        try {
            if (mc.thePlayer.sendQueue == null) return;
            NetworkManager net = mc.thePlayer.sendQueue.getNetworkManager();
            ChannelPipeline pipeline = net.channel().pipeline();
            if (pipeline.get(HANDLER_KEY) == null) {
                pipeline.addBefore("packet_handler", HANDLER_KEY, new ParticleDetector());
                EnoughFishing.LOG.info("[MEF] ParticleDetector injected.");
            }
        } catch (Exception ignored) {}
    }
}
