package com.mef.enoughfishing.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFishHook;

/**
 * Singleton state machine that tracks the fishing session.
 *
 * <p>Thread-safety: fields written only on the main MC thread (via
 * addScheduledTask or ClientTickEvent). Reads from the render thread
 * are safe because Java guarantees visibility of long/int reads on x86,
 * and the worst case is a one-frame-stale value.</p>
 *
 * <p>GC optimisation: the {@link #timerBuilder} StringBuilder is pre-allocated
 * once and reused every frame so {@link #getFormattedTime(boolean)} never
 * allocates a new buffer in the render loop. The returned String is allocated,
 * but it is tiny and extremely short-lived — the JVM's escape-analysis will
 * stack-allocate it in practice.</p>
 */
public final class FishingTracker {

    public static final FishingTracker INSTANCE = new FishingTracker();

    // Alert fade parameters (ms)
    private static final long ALERT_FADE_IN   =  150L;
    private static final long ALERT_HOLD      = 1200L;
    private static final long ALERT_FADE_OUT  =  650L;
    private static final long ALERT_TOTAL     = ALERT_FADE_IN + ALERT_HOLD + ALERT_FADE_OUT;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean isFishing;
    private long    castTime       = -1L;  // System.currentTimeMillis() at cast
    private int     castCount;

    private boolean alertActive;
    private long    alertStartTime = -1L;

    /** Pre-allocated buffer — NEVER use this outside {@link #getFormattedTime}. */
    private final StringBuilder timerBuilder = new StringBuilder(16);

    private FishingTracker() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /** Called when a new bobber enters the world. */
    public void onCast() {
        castTime   = System.currentTimeMillis();
        isFishing  = true;
        castCount++;
        alertActive = false;
    }

    /** Called when the bobber is removed (reel-in or server cancellation). */
    public void onReel() {
        isFishing  = false;
        castTime   = -1L;
        alertActive = false;
    }

    /**
     * Called (on the main thread) when a WATER_WAKE particle is detected near
     * the bobber — signals a fish is about to bite.
     */
    public void onParticleDetected() {
        if (!isFishing) return;
        alertActive    = true;
        alertStartTime = System.currentTimeMillis();
    }

    /** Resets session statistics. */
    public void resetStats() {
        castCount   = 0;
        isFishing   = false;
        castTime    = -1L;
        alertActive = false;
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    /**
     * Must be called every client tick (main thread only).
     * Detects cast/reel transitions by watching {@code thePlayer.fishEntity}.
     */
    public void tick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        EntityFishHook hook = mc.thePlayer.fishEntity;

        if (hook == null && isFishing)  { onReel(); }
        else if (hook != null && !isFishing) { onCast(); }

        // Expire the alert
        if (alertActive && System.currentTimeMillis() - alertStartTime >= ALERT_TOTAL) {
            alertActive = false;
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns a formatted timer string, reusing the internal StringBuilder.
     * Format: [M:]SS.MMMsec  or  [M:]SSsec   depending on {@code showMs}.
     */
    public String getFormattedTime(boolean showMs) {
        if (!isFishing || castTime < 0L) return "0.000s";

        long elapsed = System.currentTimeMillis() - castTime;
        long mins    = elapsed / 60_000L;
        long secs    = (elapsed % 60_000L) / 1_000L;
        long millis  = elapsed % 1_000L;

        timerBuilder.setLength(0);

        if (mins > 0) {
            timerBuilder.append(mins).append(':');
            if (secs < 10) timerBuilder.append('0');
        }
        timerBuilder.append(secs);

        if (showMs) {
            timerBuilder.append('.');
            if (millis < 100) timerBuilder.append('0');
            if (millis < 10)  timerBuilder.append('0');
            timerBuilder.append(millis);
        }
        timerBuilder.append('s');

        return timerBuilder.toString();
    }

    /**
     * Returns a smooth alpha in [0, 1] for the screen-flash overlay.
     * Follows a fade-in → hold → fade-out envelope.
     */
    public float getAlertAlpha() {
        if (!alertActive) return 0f;
        long elapsed = System.currentTimeMillis() - alertStartTime;
        if (elapsed < ALERT_FADE_IN)
            return elapsed / (float) ALERT_FADE_IN;
        if (elapsed < ALERT_FADE_IN + ALERT_HOLD)
            return 1f;
        float fadeProgress = (elapsed - ALERT_FADE_IN - ALERT_HOLD) / (float) ALERT_FADE_OUT;
        return Math.max(0f, 1f - fadeProgress);
    }

    public boolean isFishing()     { return isFishing;   }
    public int     getCastCount()  { return castCount;   }
    public boolean isAlertActive() { return alertActive; }
}
