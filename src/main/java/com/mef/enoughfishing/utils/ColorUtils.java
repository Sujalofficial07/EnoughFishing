package com.mef.enoughfishing.utils;

/**
 * Stateless color math helpers — all methods operate on packed {@code int}
 * colors (ARGB or RGB) using pure bit arithmetic to avoid creating
 * {@code java.awt.Color} objects anywhere in the hot path.
 */
public final class ColorUtils {

    private ColorUtils() {}

    // ── Packing ───────────────────────────────────────────────────────────────

    public static int fromRGB (int r, int g, int b)        { return (r << 16) | (g << 8) | b; }
    public static int fromARGB(int a, int r, int g, int b) { return (a << 24) | fromRGB(r, g, b); }
    public static int withAlpha(int rgb, int alpha)        { return (alpha << 24) | (rgb & 0x00FFFFFF); }

    // ── Component extraction ──────────────────────────────────────────────────

    public static int getAlpha(int c) { return (c >> 24) & 0xFF; }
    public static int getRed  (int c) { return (c >> 16) & 0xFF; }
    public static int getGreen(int c) { return (c >>  8) & 0xFF; }
    public static int getBlue (int c) { return  c        & 0xFF; }

    // ── Blend ─────────────────────────────────────────────────────────────────

    /**
     * Linear interpolation between two RGB colors.
     * @param t 0.0 → color1, 1.0 → color2
     */
    public static int blendRGB(int color1, int color2, float t) {
        int r = lerp(getRed(color1),   getRed(color2),   t);
        int g = lerp(getGreen(color1), getGreen(color2), t);
        int b = lerp(getBlue(color1),  getBlue(color2),  t);
        return fromRGB(r, g, b);
    }

    // ── Rainbow ───────────────────────────────────────────────────────────────

    /**
     * HSV hue-cycle color without {@code java.awt.Color}.
     * @param timeMs  {@link System#currentTimeMillis()} for animation
     * @param offset  phase offset in degrees [0, 360]
     */
    public static int rainbowRGB(long timeMs, int offset) {
        float hue  = ((timeMs / 1000f) + offset / 360f) % 1f;
        float h6   = hue * 6f;
        int   hi   = (int) h6;
        float f    = h6 - hi;
        float q    = 1f - f;
        int r, g, b;
        switch (hi % 6) {
            case 0:  r = 255; g = fi(f); b = 0;    break;
            case 1:  r = fi(q); g = 255; b = 0;    break;
            case 2:  r = 0;   g = 255; b = fi(f);  break;
            case 3:  r = 0;   g = fi(q); b = 255;  break;
            case 4:  r = fi(f); g = 0;   b = 255;  break;
            default: r = 255; g = 0;   b = fi(q);  break;
        }
        return fromRGB(r, g, b);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static int lerp(int a, int b, float t) { return a + (int)((b - a) * t); }
    private static int fi  (float f)               { return (int)(f * 255f);         }
}
