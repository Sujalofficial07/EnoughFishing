package com.mef.enoughfishing.core;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

/**
 * Wraps ForgeGradle's {@link Configuration} with typed getters and setters.
 * All values survive game restarts via the .cfg file in the mods/config folder.
 */
public final class Config {

    // ── Category keys ────────────────────────────────────────────────────────
    private static final String CAT_TIMER   = "timer";
    private static final String CAT_ALERTS  = "alerts";
    private static final String CAT_DISPLAY = "display";

    private final Configuration cfg;

    // ── Timer ─────────────────────────────────────────────────────────────────
    private boolean timerEnabled;
    private int     timerColor;        // packed RGB, no alpha
    private boolean showMilliseconds;
    private int     hudX;
    private int     hudY;
    private float   hudOpacity;        // 0.0 – 1.0

    // ── Alerts ────────────────────────────────────────────────────────────────
    private boolean particleAlertsEnabled;
    private int     alertColor;        // packed RGB
    private boolean soundAlertEnabled;
    private boolean screenFlashEnabled;
    private float   particleSensitivity; // detection radius in blocks

    // ── Display ───────────────────────────────────────────────────────────────
    private boolean showCastCount;

    public Config(File file) {
        cfg = new Configuration(file);
        load();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public void load() {
        cfg.load();

        timerEnabled     = cfg.getBoolean("enabled",          CAT_TIMER,   true,  "Show the fishing timer HUD.");
        timerColor       = cfg.getInt    ("color",            CAT_TIMER,   0xFFFFFF, 0x000000, 0xFFFFFF, "Timer text color (hex RGB).");
        showMilliseconds = cfg.getBoolean("showMilliseconds", CAT_TIMER,   true,  "Append .NNNs to the timer.");
        hudX             = cfg.getInt    ("hudX",             CAT_TIMER,   10, 0, 10000, "HUD X position in pixels.");
        hudY             = cfg.getInt    ("hudY",             CAT_TIMER,   10, 0, 10000, "HUD Y position in pixels.");
        hudOpacity       = (float) cfg.get(CAT_TIMER, "hudOpacity", 1.0, "HUD background opacity (0.0 – 1.0).").getDouble(1.0);

        particleAlertsEnabled = cfg.getBoolean("enabled",      CAT_ALERTS, true,  "Alert when WATER_WAKE particles appear near the bobber.");
        alertColor            = cfg.getInt    ("color",        CAT_ALERTS, 0xFF4444, 0x000000, 0xFFFFFF, "Screen-flash color (hex RGB).");
        soundAlertEnabled     = cfg.getBoolean("soundAlert",   CAT_ALERTS, true,  "Play a sound on bite alert.");
        screenFlashEnabled    = cfg.getBoolean("screenFlash",  CAT_ALERTS, true,  "Flash screen edges on bite alert.");
        particleSensitivity   = (float) cfg.get(CAT_ALERTS, "sensitivity", 2.0, "Detection radius around bobber in blocks (1.0 – 5.0).").getDouble(2.0);

        showCastCount = cfg.getBoolean("showCastCount", CAT_DISPLAY, true, "Show total casts on the HUD.");

        if (cfg.hasChanged()) cfg.save();
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    public void save() {
        set(CAT_TIMER, "enabled",          timerEnabled);
        set(CAT_TIMER, "color",            timerColor);
        set(CAT_TIMER, "showMilliseconds", showMilliseconds);
        set(CAT_TIMER, "hudX",             hudX);
        set(CAT_TIMER, "hudY",             hudY);
        set(CAT_TIMER, "hudOpacity",       (double) hudOpacity);

        set(CAT_ALERTS, "enabled",      particleAlertsEnabled);
        set(CAT_ALERTS, "color",        alertColor);
        set(CAT_ALERTS, "soundAlert",   soundAlertEnabled);
        set(CAT_ALERTS, "screenFlash",  screenFlashEnabled);
        set(CAT_ALERTS, "sensitivity",  (double) particleSensitivity);

        set(CAT_DISPLAY, "showCastCount", showCastCount);

        cfg.save();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void set(String cat, String key, boolean v) { cfg.getCategory(cat).get(key).set(v); }
    private void set(String cat, String key, int     v) { cfg.getCategory(cat).get(key).set(v); }
    private void set(String cat, String key, double  v) { cfg.getCategory(cat).get(key).set(v); }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public boolean isTimerEnabled()          { return timerEnabled; }
    public void    setTimerEnabled(boolean v){ timerEnabled = v; }

    public int  getTimerColor()          { return timerColor; }
    public void setTimerColor(int v)     { timerColor = v; }

    public boolean isShowMilliseconds()          { return showMilliseconds; }
    public void    setShowMilliseconds(boolean v){ showMilliseconds = v; }

    public int  getHudX()      { return hudX; }
    public void setHudX(int v) { hudX = v; }

    public int  getHudY()      { return hudY; }
    public void setHudY(int v) { hudY = v; }

    public float getHudOpacity()         { return hudOpacity; }
    public void  setHudOpacity(float v)  { hudOpacity = Math.max(0f, Math.min(1f, v)); }

    public boolean isParticleAlertsEnabled()          { return particleAlertsEnabled; }
    public void    setParticleAlertsEnabled(boolean v){ particleAlertsEnabled = v; }

    public int  getAlertColor()      { return alertColor; }
    public void setAlertColor(int v) { alertColor = v; }

    public boolean isSoundAlertEnabled()          { return soundAlertEnabled; }
    public void    setSoundAlertEnabled(boolean v){ soundAlertEnabled = v; }

    public boolean isScreenFlashEnabled()          { return screenFlashEnabled; }
    public void    setScreenFlashEnabled(boolean v){ screenFlashEnabled = v; }

    public float getParticleSensitivity()        { return particleSensitivity; }
    public void  setParticleSensitivity(float v) { particleSensitivity = Math.max(1f, Math.min(5f, v)); }

    public boolean isShowCastCount()          { return showCastCount; }
    public void    setShowCastCount(boolean v){ showCastCount = v; }
}
