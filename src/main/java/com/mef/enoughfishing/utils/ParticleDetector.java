package com.mef.enoughfishing.utils;

import com.mef.enoughfishing.EnoughFishing;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumParticleTypes;

/**
 * Netty handler that intercepts WATER_WAKE particle packets and classifies
 * them into two zones relative to the fishing bobber:
 *
 *   Outer zone  (approaching): particle within outerRadius = sensitivity + 1.5 blocks
 *   Inner zone  (arrived):     particle within innerRadius = sensitivity × 0.5 blocks
 *
 * The outer zone fires onApproachingDetected() → yellow ! on the label.
 * The inner zone fires onArrivedDetected()     → red !! + screen flash.
 *
 * Both calls are dispatched to the main thread via addScheduledTask so
 * FishingTracker state is only mutated on the main thread.
 */
public final class ParticleDetector extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof S2APacketParticles) {
                checkParticle((S2APacketParticles) msg);
            }
        } catch (Exception e) {
            EnoughFishing.LOG.warn("[MEF] ParticleDetector error: {}", e.getMessage());
        }
        super.channelRead(ctx, msg);
    }

    private void checkParticle(S2APacketParticles pkt) {
        if (pkt.getParticleType() != EnumParticleTypes.WATER_WAKE) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        EntityFishHook hook = mc.thePlayer.fishEntity;
        if (hook == null) return;

        Config cfg = EnoughFishing.INSTANCE.getConfig();
        if (!cfg.isParticleAlertsEnabled()) return;

        double dx = pkt.getXCoordinate() - hook.posX;
        double dy = pkt.getYCoordinate() - hook.posY;
        double dz = pkt.getZCoordinate() - hook.posZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        float  sens       = cfg.getParticleSensitivity();
        double outerRadSq = (sens + 1.5) * (sens + 1.5);
        double innerRadSq = (sens * 0.5 + 0.3) * (sens * 0.5 + 0.3);

        if (distSq > outerRadSq) return; // outside both zones

        if (distSq <= innerRadSq) {
            // ── ARRIVED ──────────────────────────────────────────────────────
            mc.addScheduledTask(() -> {
                FishingTracker.INSTANCE.onArrivedDetected();
                if (cfg.isSoundAlertEnabled() && mc.thePlayer != null) {
                    mc.thePlayer.playSound("random.orb", 0.65f, 1.4f);
                }
            });
        } else {
            // ── APPROACHING ───────────────────────────────────────────────────
            mc.addScheduledTask(FishingTracker.INSTANCE::onApproachingDetected);
        }
    }
}
