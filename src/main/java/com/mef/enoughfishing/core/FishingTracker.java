package com.mef.enoughfishing.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFishHook;

/**
 * Central state machine for one fishing session.
 *
 * Alert model (no flickering):
 *   APPROACHING — a particle entered the outer detection zone.
 *                 State is held for HOLD_APPROACHING ms even if no new
 *                 particles arrive, preventing rapid flicker as particles
 *                 spawn/despawn.
 *   ARRIVED     — a particle entered the inner (bite) zone.
 *                 Takes priority over APPROACHING; also held for HOLD_ARRIVED.
 *
 * Timer precision: 2 centisecond digits (e.g. "3.24s") via integer division.
 * The pre-allocated StringBuilder means getFormattedTime() never allocates
 * a buffer in the render loop — only the final tiny String is created.
 */
public final class FishingTracker {

    public static final FishingTracker INSTANCE = new FishingTracker();

    public enum AlertState { NONE, APPROACHING, ARRIVED }

    // ── Alert hold durations (ms) ─────────────────────────────────────────────
    private static final long HOLD_APPROACHING = 1500L;
    private static final long HOLD_ARRIVED     =  900L;

    // ── Screen-flash envelope (ms) ────────────────────────────────────────────
    private static final long FLASH_FADE_IN  =  120L;
    private static final long FLASH_HOLD     =  900L;
    private static final long FLASH_FADE_OUT =  600L;
    private static final long FLASH_TOTAL    = FLASH_FADE_IN + FLASH_HOLD + FLASH_FADE_OUT;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean isFishing;
    private long    castTime        = -1L;
    private int     castCount;

    private long    lastApproachMs  = -1L;
    private long    lastArrivedMs   = -1L;

    private boolean flashActive;
    private long    flashStartMs    = -1L;

    // Pre-allocated — never reallocated after construction.
    private final StringBuilder timerBuf = new StringBuilder(12);

    private FishingTracker() {}

    // ── Session events ────────────────────────────────────────────────────────

    public void onCast() {
        castTime        = System.currentTimeMillis();
        isFishing       = true;
        castCount++;
        lastApproachMs  = -1L;
        lastArrivedMs   = -1L;
        flashActive     = false;
    }

    public void onReel() {
        isFishing       = false;
        castTime        = -1L;
        lastApproachMs  = -1L;
        lastArrivedMs   = -1L;
        flashActive     = false;
    }

    // ── Alert events (main thread only) ──────────────────────────────────────

    /** Particle entered outer zone → start / refresh APPROACHING hold. */
    public void onApproachingDetected() {
        if (!isFishing) return;
        lastApproachMs = System.currentTimeMillis();
    }

    /** Particle entered inner zone → ARRIVED takes over, triggers screen flash. */
    public void onArrivedDetected() {
        if (!isFishing) return;
        long now       = System.currentTimeMillis();
        lastArrivedMs  = now;
        lastApproachMs = now;   // also refresh APPROACHING
        flashActive    = true;
        flashStartMs   = now;
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    public void tick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        EntityFishHook hook = mc.thePlayer.fishEntity;
        if      (hook == null &&  isFishing) { onReel(); }
        else if (hook != null && !isFishing) { onCast(); }

        if (flashActive && System.currentTimeMillis() - flashStartMs >= FLASH_TOTAL) {
            flashActive = false;
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public AlertState getAlertState() {
        if (!isFishing) return AlertState.NONE;
        long now = System.currentTimeMillis();
        if (lastArrivedMs  > 0 && now - lastArrivedMs  < HOLD_ARRIVED)     return AlertState.ARRIVED;
        if (lastApproachMs > 0 && now - lastApproachMs < HOLD_APPROACHING) return AlertState.APPROACHING;
        return AlertState.NONE;
    }

    /**
     * Smooth sinusoidal pulse factor in [0.65 – 1.0] keyed to wall-clock time.
     * Used to animate the floating bobber label — no frame-count dependency,
     * so it never stutters during server lag.
     */
    public float getAlertPulse() {
        long now = System.currentTimeMillis();
        switch (getAlertState()) {
            case APPROACHING: return 0.78f + 0.22f * (float) Math.sin(now * 0.004);
            case ARRIVED:     return 0.65f + 0.35f * (float) Math.abs(Math.sin(now * 0.010));
            default:          return 1.0f;
        }
    }

    /** [0,1] alpha for the screen-flash overlay (fade-in → hold → fade-out). */
    public float getFlashAlpha() {
        if (!flashActive) return 0f;
        long e = System.currentTimeMillis() - flashStartMs;
        if (e >= FLASH_TOTAL)     return 0f;
        if (e < FLASH_FADE_IN)    return e / (float) FLASH_FADE_IN;
        if (e < FLASH_FADE_IN + FLASH_HOLD) return 1f;
        return 1f - (e - FLASH_FADE_IN - FLASH_HOLD) / (float) FLASH_FADE_OUT;
    }

    /**
     * 2-centisecond-precision timer string.  Format: [M:]SS.CCs
     * Examples: "3.24s"  "1:02.07s"
     * Zero allocations in the buffer itself; only the final toString() allocates.
     */
    public String getFormattedTime(boolean showCentis) {
        if (!isFishing || castTime < 0L) return "0.00s";
        long elapsed = System.currentTimeMillis() - castTime;
        long mins    = elapsed / 60_000L;
        long secs    = (elapsed % 60_000L) / 1_000L;
        long centis  = (elapsed % 1_000L)  / 10L;   // ← 2 digits only

        timerBuf.setLength(0);
        if (mins > 0) {
            timerBuf.append(mins).append(':');
            if (secs < 10) timerBuf.append('0');
        }
        timerBuf.append(secs);
        if (showCentis) {
            timerBuf.append('.');
            if (centis < 10) timerBuf.append('0');
            timerBuf.append(centis);
        }
        timerBuf.append('s');
        return timerBuf.toString();
    }

    public void resetStats() {
        castCount = 0; isFishing = false; castTime = -1L;
        lastApproachMs = -1L; lastArrivedMs = -1L; flashActive = false;
    }

    public boolean isFishing()     { return isFishing;  }
    public int     getCastCount()  { return castCount;  }
    public boolean isFlashActive() { return flashActive; }
}
