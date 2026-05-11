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
    private int     hudX, hudY;
    private float   hudOpacity;
    private boolean rainbowMode;

    // Alerts
    private boolean particleAlertsEnabled;
    private int     approachingColor;   // ← shown when fish approaching
    private int     arrivedColor;       // ← shown when fish has arrived
    private boolean soundAlertEnabled;
    private boolean screenFlashEnabled;
    private float   particleSensitivity;

    // Display
    private boolean showCastCount;

    public Config(File file) { cfg = new Configuration(file); load(); }

    public void load() {
        cfg.load();

        timerEnabled     = cfg.getBoolean("enabled",          CAT_TIMER, true,    "Show floating bobber timer.");
        timerColor       = cfg.getInt    ("color",            CAT_TIMER, 0x00FF88, 0, 0xFFFFFF, "Timer color (RGB).");
        showMilliseconds = cfg.getBoolean("showMilliseconds", CAT_TIMER, true,    "Show 2-digit centiseconds.");
        hudX             = cfg.getInt    ("hudX",             CAT_TIMER, 10, 0, 10000, "HUD X.");
        hudY             = cfg.getInt    ("hudY",             CAT_TIMER, 10, 0, 10000, "HUD Y.");
        hudOpacity       = (float) cfg.get(CAT_TIMER, "hudOpacity", 1.0, "Opacity 0-1.").getDouble(1.0);
        rainbowMode      = cfg.getBoolean("rainbowMode",      CAT_TIMER, false,   "Cycle hue over time.");

        particleAlertsEnabled = cfg.getBoolean("enabled",          CAT_ALERTS, true,    "Enable particle bite detection.");
        approachingColor      = cfg.getInt    ("approachingColor",  CAT_ALERTS, 0xFFCC00, 0, 0xFFFFFF, "Approaching indicator color.");
        arrivedColor          = cfg.getInt    ("arrivedColor",      CAT_ALERTS, 0xFF3333, 0, 0xFFFFFF, "Arrived indicator color.");
        soundAlertEnabled     = cfg.getBoolean("soundAlert",        CAT_ALERTS, true,    "Sound on bite.");
        screenFlashEnabled    = cfg.getBoolean("screenFlash",       CAT_ALERTS, true,    "Flash screen on bite.");
        particleSensitivity   = (float) cfg.get(CAT_ALERTS, "sensitivity", 2.0, "Detection radius (1-5 blocks).").getDouble(2.0);

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

        set(CAT_ALERTS, "enabled",          particleAlertsEnabled);
        set(CAT_ALERTS, "approachingColor", approachingColor);
        set(CAT_ALERTS, "arrivedColor",     arrivedColor);
        set(CAT_ALERTS, "soundAlert",       soundAlertEnabled);
        set(CAT_ALERTS, "screenFlash",      screenFlashEnabled);
        set(CAT_ALERTS, "sensitivity",      (double) particleSensitivity);

        set(CAT_DISPLAY, "showCastCount", showCastCount);
        cfg.save();
    }

    private void set(String c, String k, boolean v) { cfg.getCategory(c).get(k).set(v); }
    private void set(String c, String k, int     v) { cfg.getCategory(c).get(k).set(v); }
    private void set(String c, String k, double  v) { cfg.getCategory(c).get(k).set(v); }

    public boolean isTimerEnabled()                    { return timerEnabled; }
    public void    setTimerEnabled(boolean v)          { timerEnabled = v; }
    public int     getTimerColor()                     { return timerColor; }
    public void    setTimerColor(int v)                { timerColor = v; }
    public boolean isShowMilliseconds()                { return showMilliseconds; }
    public void    setShowMilliseconds(boolean v)      { showMilliseconds = v; }
    public int     getHudX()                           { return hudX; }
    public void    setHudX(int v)                      { hudX = v; }
    public int     getHudY()                           { return hudY; }
    public void    setHudY(int v)                      { hudY = v; }
    public float   getHudOpacity()                     { return hudOpacity; }
    public void    setHudOpacity(float v)              { hudOpacity = Math.max(0f, Math.min(1f, v)); }
    public boolean isRainbowMode()                     { return rainbowMode; }
    public void    setRainbowMode(boolean v)           { rainbowMode = v; }
    public boolean isParticleAlertsEnabled()           { return particleAlertsEnabled; }
    public void    setParticleAlertsEnabled(boolean v) { particleAlertsEnabled = v; }
    public int     getApproachingColor()               { return approachingColor; }
    public void    setApproachingColor(int v)          { approachingColor = v; }
    public int     getArrivedColor()                   { return arrivedColor; }
    public void    setArrivedColor(int v)              { arrivedColor = v; }
    public boolean isSoundAlertEnabled()               { return soundAlertEnabled; }
    public void    setSoundAlertEnabled(boolean v)     { soundAlertEnabled = v; }
    public boolean isScreenFlashEnabled()              { return screenFlashEnabled; }
    public void    setScreenFlashEnabled(boolean v)    { screenFlashEnabled = v; }
    public float   getParticleSensitivity()            { return particleSensitivity; }
    public void    setParticleSensitivity(float v)     { particleSensitivity = Math.max(1f, Math.min(5f, v)); }
    public boolean isShowCastCount()                   { return showCastCount; }
    public void    setShowCastCount(boolean v)         { showCastCount = v; }
}
