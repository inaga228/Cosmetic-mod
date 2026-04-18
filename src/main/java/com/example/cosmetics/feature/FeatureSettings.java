package com.example.cosmetics.feature;

/**
 * Mutable settings bag for a feature. Not every field is used by every
 * feature; see {@link FeatureType#caps}.
 */
public final class FeatureSettings {
    public float colorR = 1.0F;
    public float colorG = 1.0F;
    public float colorB = 1.0F;

    public float size = 1.0F;      // 0.25 .. 3.0
    public float density = 1.0F;   // 0.0 .. 3.0 (particles per tick multiplier)
    public float speed = 1.0F;     // 0.25 .. 3.0

    public int style = 0;
    public int count = 8;          // count of particles per event (hit effects)

    public float offsetX = 0.0F, offsetY = 0.0F, offsetZ = 0.0F; // -1 .. 1
    public float rotX = 0.0F,    rotY = 0.0F,    rotZ = 0.0F;    // -180 .. 180

    public int argb() {
        int r = clamp255((int) (colorR * 255));
        int g = clamp255((int) (colorG * 255));
        int b = clamp255((int) (colorB * 255));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int clamp255(int v) { return Math.max(0, Math.min(255, v)); }
}
