package com.mef.enoughfishing.core;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public final class Config {

    private static final String CAT_TIMER   = "timer";
    private static final String CAT_ALERTS  = "alerts";
    private static final String CAT_DISPLAY = "display";

    private final Configuration cfg;

    // Timer
    private boolean timerEnabled;
    private int     timerColor;
    private boolean showMilliseconds;
    private int     hudX;
    private int     hudY;
    private float   hudOpacity;
    private boolean rainbowMode;   // ← NEW

    // Alerts
    private boolean particleAlertsEnabled;
    private int     alertColor;
    private boolean soundAlertEnabled;
    private boolean screenFlashEnabled;
    private float   particleSensitivity;

    // Display
    private boolean showCastCount;

    public Config(File file) {
        cfg = new Configuration(file);
        load();
    }

    public void load() {
        cfg.load();

        timerEnabled     = cfg.getBoolean("enabled",          CAT_TIMER, true,  "Show the floating bobber timer.");
        timerColor       = cfg.getInt    ("color",            CAT_TIMER, 0x00FF88, 0x000000, 0xFFFFFF, "Timer text color (hex RGB).");
        showMilliseconds = cfg.getBoolean("showMilliseconds", CAT_TIMER, true,  "Show .NNNs precision.");
        hudX             = cfg.getInt    ("hudX",             CAT_TIMER, 10, 0, 10000, "Fallback HUD X.");
        hudY             = cfg.getInt    ("hudY",             CAT_TIMER, 10, 0, 10000, "Fallback HUD Y.");
        hudOpacity       = (float) cfg.get(CAT_TIMER, "hudOpacity", 1.0, "HUD background opacity (0-1).").getDouble(1.0);
        rainbowMode      = cfg.getBoolean("rainbowMode",      CAT_TIMER, false, "Cycle timer color through the full hue spectrum.");

        particleAlertsEnabled = cfg.getBoolean("enabled",     CAT_ALERTS, true,  "Detect WATER_WAKE particles near bobber.");
        alertColor            = cfg.getInt    ("color",       CAT_ALERTS, 0xFF4444, 0x000000, 0xFFFFFF, "Alert flash color.");
        soundAlertEnabled     = cfg.getBoolean("soundAlert",  CAT_ALERTS, true,  "Play sound on bite detection.");
        screenFlashEnabled    = cfg.getBoolean("screenFlash", CAT_ALERTS, true,  "Flash screen edges on bite.");
        particleSensitivity   = (float) cfg.get(CAT_ALERTS, "sensitivity", 2.0, "Detection radius in blocks (1-5).").getDouble(2.0);

        showCastCount = cfg.getBoolean("showCastCount", CAT_DISPLAY, true, "Show cast count on HUD.");

        if (cfg.hasChanged()) cfg.save();
    }

    public void save() {
        set(CAT_TIMER, "enabled",          timerEnabled);
        set(CAT_TIMER, "color",            timerColor);
        set(CAT_TIMER, "showMilliseconds", showMilliseconds);
        set(CAT_TIMER, "hudX",             hudX);
        set(CAT_TIMER, "hudY",             hudY);
        set(CAT_TIMER, "hudOpacity",       (double) hudOpacity);
        set(CAT_TIMER, "rainbowMode",      rainbowMode);

        set(CAT_ALERTS, "enabled",      particleAlertsEnabled);
        set(CAT_ALERTS, "color",        alertColor);
        set(CAT_ALERTS, "soundAlert",   soundAlertEnabled);
        set(CAT_ALERTS, "screenFlash",  screenFlashEnabled);
        set(CAT_ALERTS, "sensitivity",  (double) particleSensitivity);

        set(CAT_DISPLAY, "showCastCount", showCastCount);

        cfg.save();
    }

    private void set(String c, String k, boolean v){ cfg.getCategory(c).get(k).set(v); }
    private void set(String c, String k, int     v){ cfg.getCategory(c).get(k).set(v); }
    private void set(String c, String k, double  v){ cfg.getCategory(c).get(k).set(v); }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public boolean isTimerEnabled()                   { return timerEnabled; }
    public void    setTimerEnabled(boolean v)         { timerEnabled = v; }

    public int  getTimerColor()                       { return timerColor; }
    public void setTimerColor(int v)                  { timerColor = v; }

    public boolean isShowMilliseconds()               { return showMilliseconds; }
    public void    setShowMilliseconds(boolean v)     { showMilliseconds = v; }

    public int  getHudX()                             { return hudX; }
    public void setHudX(int v)                        { hudX = v; }

    public int  getHudY()                             { return hudY; }
    public void setHudY(int v)                        { hudY = v; }

    public float getHudOpacity()                      { return hudOpacity; }
    public void  setHudOpacity(float v)               { hudOpacity = Math.max(0f, Math.min(1f, v)); }

    public boolean isRainbowMode()                    { return rainbowMode; }
    public void    setRainbowMode(boolean v)          { rainbowMode = v; }

    public boolean isParticleAlertsEnabled()          { return particleAlertsEnabled; }
    public void    setParticleAlertsEnabled(boolean v){ particleAlertsEnabled = v; }

    public int  getAlertColor()                       { return alertColor; }
    public void setAlertColor(int v)                  { alertColor = v; }

    public boolean isSoundAlertEnabled()              { return soundAlertEnabled; }
    public void    setSoundAlertEnabled(boolean v)    { soundAlertEnabled = v; }

    public boolean isScreenFlashEnabled()             { return screenFlashEnabled; }
    public void    setScreenFlashEnabled(boolean v)   { screenFlashEnabled = v; }

    public float getParticleSensitivity()             { return particleSensitivity; }
    public void  setParticleSensitivity(float v)      { particleSensitivity = Math.max(1f, Math.min(5f, v)); }

    public boolean isShowCastCount()                  { return showCastCount; }
    public void    setShowCastCount(boolean v)        { showCastCount = v; }
}
