package com.mef.enoughfishing.utils;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.FishingTracker;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumParticleTypes;

/**
 * Netty duplex handler injected into the client-side pipeline by
 * {@link com.mef.enoughfishing.events.FishingEventHandler}.
 *
 * <p>Intercepts {@link S2APacketParticles} packets and fires
 * {@link FishingTracker#onParticleDetected()} when a
 * {@link EnumParticleTypes#WATER_WAKE} particle spawns within the
 * configured sensitivity radius of the player's bobber.</p>
 *
 * <p>All packet handling occurs on Netty's IO thread. The actual tracker
 * mutation is dispatched to the main Minecraft thread via
 * {@link Minecraft#addScheduledTask(Runnable)} to maintain thread-safety.</p>
 */
public final class ParticleDetector extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof S2APacketParticles) {
                handleParticle((S2APacketParticles) msg);
            }
        } catch (Exception e) {
            // Never swallow the packet; re-throw after logging.
            EnoughFishing.LOG.warn("[MEF] ParticleDetector error: {}", e.getMessage());
        }
        // Always pass the packet down the pipeline.
        super.channelRead(ctx, msg);
    }

    private void handleParticle(S2APacketParticles pkt) {
        // Fast-path: only care about WATER_WAKE
        if (pkt.getParticleType() != EnumParticleTypes.WATER_WAKE) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        EntityFishHook hook = mc.thePlayer.fishEntity;
        if (hook == null) return;

        // Config read is inherently safe — only primitives, no synchronization needed.
        float sensitivity = EnoughFishing.INSTANCE.getConfig().getParticleSensitivity();
        double radSq      = sensitivity * sensitivity;

        double dx = pkt.getXCoordinate() - hook.posX;
        double dy = pkt.getYCoordinate() - hook.posY;
        double dz = pkt.getZCoordinate() - hook.posZ;

        if (dx * dx + dy * dy + dz * dz > radSq) return;

        // Dispatch to main thread — mutating tracker or calling playSound off the
        // main thread would cause race conditions or crash in SoundManager.
        mc.addScheduledTask(() -> {
            FishingTracker.INSTANCE.onParticleDetected();

            if (EnoughFishing.INSTANCE.getConfig().isSoundAlertEnabled()
                    && mc.thePlayer != null) {
                // "random.orb" is the XP pickup sound — a subtle, distinct ping.
                mc.thePlayer.playSound("random.orb", 0.6f, 1.4f);
            }
        });
    }
}
