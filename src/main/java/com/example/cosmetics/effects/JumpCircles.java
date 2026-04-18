package com.example.cosmetics.effects;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.render.Primitives;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Jump / landing ring effects rendered flat on the XZ plane at the player's
 * feet. Each spawned ring stores its own style snapshot so style changes mid-
 * flight don't garble in-flight rings.
 *
 * <p>Jump styles ({@link FeatureType#JUMP_CIRCLES}, {@link FeatureSettings#style}
 * mod {@link #STYLE_COUNT}):
 * <ul>
 *   <li>0 — THIN     hairline ring, single line</li>
 *   <li>1 — THICK    bold ring drawn as 4 stacked rings (pseudo-thickness)</li>
 *   <li>2 — DOUBLE   two concentric rings expanding at different rates</li>
 *   <li>3 — RAINBOW  hue-shifted ring, animated</li>
 *   <li>4 — PULSE    ring grows then re-emits a smaller pulse halfway through</li>
 *   <li>5 — RUNIC    ring drawn as 12 short arc segments with gaps (rune-like)</li>
 * </ul>
 *
 * <p>Landing styles share the same enumeration but defaults differ.
 */
public final class JumpCircles {

    public static final int STYLE_COUNT = 6;
    public static final String[] STYLE_NAMES = {
            "Thin", "Thick", "Double", "Rainbow", "Pulse", "Runic"
    };

    private static final JumpCircles INSTANCE = new JumpCircles();
    public static JumpCircles get() { return INSTANCE; }

    private static final int HARD_CAP = 96;
    private final List<Ring> rings = new ArrayList<>();

    public void tick() {
        Iterator<Ring> it = rings.iterator();
        while (it.hasNext()) {
            Ring r = it.next();
            r.age++;
            if (r.age >= r.maxAge) it.remove();
        }
    }

    /** Called when the local player jumps. Spawns the configured jump ring(s). */
    public void spawnJump(PlayerEntity player) {
        CosmeticsState state = CosmeticsState.get();
        if (!state.isOn(FeatureType.JUMP_CIRCLES)) return;
        if (rings.size() >= HARD_CAP) return;

        FeatureSettings fs = state.settings(FeatureType.JUMP_CIRCLES);
        int style = Math.floorMod(fs.style, STYLE_COUNT);
        float size = clampPos(fs.size, 0.25F, 3.0F);
        float speedMul = clampPos(fs.speed, 0.25F, 3.0F);

        double px = player.getX();
        double py = player.getY() + 0.02;           // just above the feet
        double pz = player.getZ();

        int rgb = toRgb(fs.colorR, fs.colorG, fs.colorB);
        emit(rings, style, px, py, pz, size, speedMul, rgb, /* landing= */ false);
    }

    /** Called when the local player lands on the ground after being airborne. */
    public void spawnLanding(PlayerEntity player, float fallDistance) {
        CosmeticsState state = CosmeticsState.get();
        if (!state.isOn(FeatureType.LANDING_RING)) return;
        if (fallDistance < 0.6F) return;           // skip micro-landings
        if (rings.size() >= HARD_CAP) return;

        FeatureSettings fs = state.settings(FeatureType.LANDING_RING);
        int style = Math.floorMod(fs.style, STYLE_COUNT);
        // Landing scales with fall distance so big drops make bigger rings.
        float intensity = Math.min(1.0F, fallDistance / 5.0F);
        float size = clampPos(fs.size, 0.25F, 3.0F) * (0.7F + 1.0F * intensity);

        double px = player.getX();
        double py = player.getY() + 0.02;
        double pz = player.getZ();

        int rgb = toRgb(fs.colorR, fs.colorG, fs.colorB);
        // Landing rings expand a bit slower (heavier feel).
        emit(rings, style, px, py, pz, size, 0.85F, rgb, /* landing= */ true);
    }

    private static void emit(List<Ring> out, int style,
                             double px, double py, double pz,
                             float size, float speedMul, int rgb, boolean landing) {
        switch (style) {
            case 0: { // THIN — single hairline ring
                int life = (int) (22 / speedMul);
                out.add(mk(px, py, pz, 0.30F * size, 2.4F * size, life, rgb, style, false, 0, landing));
                break;
            }
            case 1: { // THICK — bold ring
                int life = (int) (24 / speedMul);
                out.add(mk(px, py, pz, 0.30F * size, 2.5F * size, life, rgb, style, false, 0, landing));
                break;
            }
            case 2: { // DOUBLE — two concentric rings, different rates
                int life = (int) (26 / speedMul);
                out.add(mk(px, py, pz, 0.35F * size, 2.8F * size, life, rgb, style, false, 0, landing));
                out.add(mk(px, py, pz, 0.20F * size, 1.6F * size, (int) (life * 0.7F), rgb, style, false, 0, landing));
                break;
            }
            case 3: { // RAINBOW
                int life = (int) (28 / speedMul);
                out.add(mk(px, py, pz, 0.30F * size, 2.6F * size, life, rgb, style, true, 0, landing));
                break;
            }
            case 4: { // PULSE — ring + delayed inner pulse
                int life = (int) (30 / speedMul);
                out.add(mk(px, py, pz, 0.30F * size, 2.7F * size, life, rgb, style, false, 0, landing));
                out.add(mk(px, py, pz, 0.20F * size, 1.5F * size, (int) (life * 0.55F), rgb, style, false,
                        (int) (life * 0.40F), landing));
                break;
            }
            case 5: default: { // RUNIC — segmented ring (gaps every other segment)
                int life = (int) (28 / speedMul);
                out.add(mk(px, py, pz, 0.40F * size, 2.5F * size, life, rgb, style, false, 0, landing));
                break;
            }
        }
    }

    private static Ring mk(double x, double y, double z, float startR, float endR, int maxAge,
                           int rgb, int style, boolean rainbow, int delay, boolean landing) {
        Ring r = new Ring(x, y, z, startR, endR, maxAge, rgb, style, rainbow);
        r.delay = delay;
        r.landing = landing;
        return r;
    }

    public void renderAll(MatrixStack ms, float partialTicks) {
        if (rings.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();
        IRenderTypeBuffer.Impl buf = mc.renderBuffers().bufferSource();
        IVertexBuilder vb = buf.getBuffer(RenderType.lines());

        long now = System.currentTimeMillis();
        float hueBase = (now % 4000L) / 4000.0F;

        for (Ring r : rings) {
            if (r.age < r.delay) continue; // pulse second ring waits for delay
            int effAge = r.age - r.delay;
            float life = (effAge + partialTicks) / (float) (r.maxAge - r.delay);
            if (life < 0F) life = 0F; else if (life > 1F) life = 1F;

            float radius = r.startR + (r.endR - r.startR) * easeOut(life);
            int alpha = (int)(Math.max(0F, 1F - life) * 220);
            if (r.landing) alpha = Math.min(255, alpha + 25);
            if (alpha <= 4) continue;

            int rgb = r.style == 3 // RAINBOW
                    ? hsvRgb(hueBase + life * 0.5F, 0.85F, 1.0F)
                    : r.rgb;
            int cr = (rgb >> 16) & 0xFF;
            int cg = (rgb >>  8) & 0xFF;
            int cb =  rgb        & 0xFF;

            ms.pushPose();
            ms.translate(r.x - cam.x, r.y - cam.y, r.z - cam.z);

            int segs = 56;
            switch (r.style) {
                case 1: { // THICK — 4 stacked rings, slight radius offset
                    float[] offs = { -0.04F, 0.00F, 0.04F, 0.08F };
                    for (float o : offs) hollowRing(ms, vb, radius + o, segs, cr, cg, cb, alpha);
                    break;
                }
                case 5: { // RUNIC — segmented (12 arcs of ~8 segs, alternating gaps)
                    int arcs = 12;
                    int segPerArc = 6;
                    for (int aIdx = 0; aIdx < arcs; aIdx++) {
                        if ((aIdx & 1) == 1) continue; // gap
                        double aStart = aIdx * (Math.PI * 2 / arcs);
                        double aEnd = aStart + (Math.PI * 2 / arcs) * 0.85; // small gap
                        for (int i = 0; i < segPerArc; i++) {
                            double t0 = aStart + (aEnd - aStart) * i / segPerArc;
                            double t1 = aStart + (aEnd - aStart) * (i + 1) / segPerArc;
                            float x0 = (float)(Math.cos(t0) * radius);
                            float z0 = (float)(Math.sin(t0) * radius);
                            float x1 = (float)(Math.cos(t1) * radius);
                            float z1 = (float)(Math.sin(t1) * radius);
                            Primitives.line(ms, vb, x0, 0F, z0, x1, 0F, z1, cr, cg, cb, alpha);
                        }
                    }
                    // small inner runic ring at half-radius
                    if (radius > 0.4F) hollowRing(ms, vb, radius * 0.55F, segs, cr, cg, cb, alpha / 2);
                    break;
                }
                case 2: // DOUBLE — single ring per Ring, second ring is its own list entry
                case 3: // RAINBOW — single ring
                case 4: // PULSE — ditto
                case 0: // THIN
                default: {
                    hollowRing(ms, vb, radius, segs, cr, cg, cb, alpha);
                    if (r.style != 0 && radius > 0.25F) {
                        // subtle inner depth for non-thin styles
                        hollowRing(ms, vb, radius * 0.92F, segs, cr, cg, cb, alpha / 3);
                    }
                    break;
                }
            }

            ms.popPose();
        }

        buf.endBatch(RenderType.lines());
    }

    private static void hollowRing(MatrixStack ms, IVertexBuilder vb, float radius,
                                    int segs, int r, int g, int b, int a) {
        for (int i = 0; i < segs; i++) {
            double t0 = i * Math.PI * 2 / segs;
            double t1 = (i + 1) * Math.PI * 2 / segs;
            float x0 = (float)(Math.cos(t0) * radius);
            float z0 = (float)(Math.sin(t0) * radius);
            float x1 = (float)(Math.cos(t1) * radius);
            float z1 = (float)(Math.sin(t1) * radius);
            Primitives.line(ms, vb, x0, 0F, z0, x1, 0F, z1, r, g, b, a);
        }
    }

    private static float easeOut(float t) { return 1F - (1F - t) * (1F - t); }

    private static int toRgb(float r, float g, float b) {
        int ir = clamp((int)(r * 255));
        int ig = clamp((int)(g * 255));
        int ib = clamp((int)(b * 255));
        return (ir << 16) | (ig << 8) | ib;
    }

    /**
     * HSV → RGB packed int. Inlined here so the effects package does not need
     * to depend on the gui package (avoids a render-thread / gui crosswire).
     */
    private static int hsvRgb(float h, float s, float v) {
        h = ((h % 1F) + 1F) % 1F;
        int i = (int) (h * 6F);
        float f = h * 6F - i;
        float p = v * (1F - s);
        float q = v * (1F - f * s);
        float t = v * (1F - (1F - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q;
        }
        int ir = clamp((int)(r * 255));
        int ig = clamp((int)(g * 255));
        int ib = clamp((int)(b * 255));
        return (ir << 16) | (ig << 8) | ib;
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    private static float clampPos(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private JumpCircles() {}

    private static final class Ring {
        final double x, y, z;
        final float startR, endR;
        final int maxAge;
        final int rgb;
        final int style;
        final boolean rainbow;
        boolean landing;
        int delay;
        int age;

        Ring(double x, double y, double z, float startR, float endR, int maxAge,
             int rgb, int style, boolean rainbow) {
            this.x = x; this.y = y; this.z = z;
            this.startR = startR; this.endR = endR;
            this.maxAge = maxAge; this.rgb = rgb;
            this.style = style; this.rainbow = rainbow;
        }
    }
}
