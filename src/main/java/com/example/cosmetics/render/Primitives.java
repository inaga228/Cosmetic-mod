package com.example.cosmetics.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * Low-level helpers for drawing 3D geometry in the world. All primitives
 * are drawn as line strips using the built-in {@link RenderType#lines()}
 * render type — fast, textureless, works with per-vertex color.
 */
public final class Primitives {

    public static IVertexBuilder lineBuffer(IRenderTypeBuffer buf) {
        return buf.getBuffer(RenderType.lines());
    }

    // ---- Low-level ----------------------------------------------------------

    public static void line(MatrixStack ms, IVertexBuilder vb,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            int r, int g, int b, int a) {
        Matrix4f pose = ms.last().pose();
        Matrix3f normal = ms.last().normal();
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6F) { dx = 0; dy = 1; dz = 0; } else { dx /= len; dy /= len; dz /= len; }
        vb.vertex(pose, x1, y1, z1).color(r, g, b, a).normal(normal, dx, dy, dz).endVertex();
        vb.vertex(pose, x2, y2, z2).color(r, g, b, a).normal(normal, dx, dy, dz).endVertex();
    }

    // ---- Shapes -------------------------------------------------------------

    /** Wireframe cube centered at origin, edge length s. */
    public static void cube(MatrixStack ms, IVertexBuilder vb, float s, int r, int g, int b, int a) {
        float h = s / 2f;
        // Bottom square
        line(ms, vb, -h,-h,-h,  h,-h,-h, r,g,b,a);
        line(ms, vb,  h,-h,-h,  h,-h, h, r,g,b,a);
        line(ms, vb,  h,-h, h, -h,-h, h, r,g,b,a);
        line(ms, vb, -h,-h, h, -h,-h,-h, r,g,b,a);
        // Top square
        line(ms, vb, -h, h,-h,  h, h,-h, r,g,b,a);
        line(ms, vb,  h, h,-h,  h, h, h, r,g,b,a);
        line(ms, vb,  h, h, h, -h, h, h, r,g,b,a);
        line(ms, vb, -h, h, h, -h, h,-h, r,g,b,a);
        // Vertical edges
        line(ms, vb, -h,-h,-h, -h, h,-h, r,g,b,a);
        line(ms, vb,  h,-h,-h,  h, h,-h, r,g,b,a);
        line(ms, vb,  h,-h, h,  h, h, h, r,g,b,a);
        line(ms, vb, -h,-h, h, -h, h, h, r,g,b,a);
    }

    /** Wireframe regular tetrahedron centered at origin, circumradius s. */
    public static void tetra(MatrixStack ms, IVertexBuilder vb, float s, int r, int g, int b, int a) {
        float h = s;
        // Vertices of a tetrahedron.
        float[] a1 = { h,  h,  h};
        float[] a2 = { h, -h, -h};
        float[] a3 = {-h,  h, -h};
        float[] a4 = {-h, -h,  h};
        line(ms, vb, a1[0],a1[1],a1[2], a2[0],a2[1],a2[2], r,g,b,a);
        line(ms, vb, a1[0],a1[1],a1[2], a3[0],a3[1],a3[2], r,g,b,a);
        line(ms, vb, a1[0],a1[1],a1[2], a4[0],a4[1],a4[2], r,g,b,a);
        line(ms, vb, a2[0],a2[1],a2[2], a3[0],a3[1],a3[2], r,g,b,a);
        line(ms, vb, a2[0],a2[1],a2[2], a4[0],a4[1],a4[2], r,g,b,a);
        line(ms, vb, a3[0],a3[1],a3[2], a4[0],a4[1],a4[2], r,g,b,a);
    }

    /** 6-point 3D star (3 axis-aligned crossing bars). */
    public static void star(MatrixStack ms, IVertexBuilder vb, float s, int r, int g, int b, int a) {
        line(ms, vb, -s,0,0,  s,0,0, r,g,b,a);
        line(ms, vb, 0,-s,0,  0,s,0, r,g,b,a);
        line(ms, vb, 0,0,-s,  0,0,s, r,g,b,a);
    }

    /** Low-poly sphere approximated by 2 orthogonal octagons (latitude-like). */
    public static void sphere(MatrixStack ms, IVertexBuilder vb, float radius, int r, int g, int b, int a) {
        int sides = 10;
        // XZ plane
        for (int i = 0; i < sides; i++) {
            double t0 = i * Math.PI * 2 / sides;
            double t1 = (i + 1) * Math.PI * 2 / sides;
            line(ms, vb,
                    (float)(Math.cos(t0)*radius), 0, (float)(Math.sin(t0)*radius),
                    (float)(Math.cos(t1)*radius), 0, (float)(Math.sin(t1)*radius),
                    r,g,b,a);
        }
        // XY plane
        for (int i = 0; i < sides; i++) {
            double t0 = i * Math.PI * 2 / sides;
            double t1 = (i + 1) * Math.PI * 2 / sides;
            line(ms, vb,
                    (float)(Math.cos(t0)*radius), (float)(Math.sin(t0)*radius), 0,
                    (float)(Math.cos(t1)*radius), (float)(Math.sin(t1)*radius), 0,
                    r,g,b,a);
        }
        // YZ plane
        for (int i = 0; i < sides; i++) {
            double t0 = i * Math.PI * 2 / sides;
            double t1 = (i + 1) * Math.PI * 2 / sides;
            line(ms, vb,
                    0, (float)(Math.cos(t0)*radius), (float)(Math.sin(t0)*radius),
                    0, (float)(Math.cos(t1)*radius), (float)(Math.sin(t1)*radius),
                    r,g,b,a);
        }
    }

    /** Snowflake: 6 arms with small fork tips. */
    public static void snowflake(MatrixStack ms, IVertexBuilder vb, float s, int r, int g, int b, int a) {
        for (int i = 0; i < 6; i++) {
            double t = i * Math.PI / 3;
            float cx = (float) Math.cos(t), cz = (float) Math.sin(t);
            line(ms, vb, 0,0,0, cx*s, 0, cz*s, r,g,b,a);
            // fork tip
            float forkStart = s * 0.6F;
            float forkLen   = s * 0.3F;
            double tA = t + Math.PI / 6;
            double tB = t - Math.PI / 6;
            line(ms, vb,
                    (float)Math.cos(t)*forkStart, 0, (float)Math.sin(t)*forkStart,
                    (float)Math.cos(t)*forkStart + (float)Math.cos(tA)*forkLen, 0,
                    (float)Math.sin(t)*forkStart + (float)Math.sin(tA)*forkLen,
                    r,g,b,a);
            line(ms, vb,
                    (float)Math.cos(t)*forkStart, 0, (float)Math.sin(t)*forkStart,
                    (float)Math.cos(t)*forkStart + (float)Math.cos(tB)*forkLen, 0,
                    (float)Math.sin(t)*forkStart + (float)Math.sin(tB)*forkLen,
                    r,g,b,a);
        }
    }

    /** 2D heart outline (in XY plane). */
    public static void heart(MatrixStack ms, IVertexBuilder vb, float s, int r, int g, int b, int a) {
        int segs = 24;
        float[] px = new float[segs + 1];
        float[] py = new float[segs + 1];
        for (int i = 0; i <= segs; i++) {
            double t = i * Math.PI * 2 / segs;
            // Classic parametric heart (scaled)
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            px[i] = (float) (x * s / 20);
            py[i] = (float) (y * s / 20);
        }
        for (int i = 0; i < segs; i++) {
            line(ms, vb, px[i], py[i], 0, px[i + 1], py[i + 1], 0, r, g, b, a);
        }
    }

    /** Short diagonal slash (a thick-looking X of 3 parallel lines). */
    public static void slash(MatrixStack ms, IVertexBuilder vb, float s, int r, int g, int b, int a) {
        for (int i = -1; i <= 1; i++) {
            float off = i * 0.05F * s;
            line(ms, vb, -s + off, -s, 0, s + off, s, 0, r, g, b, a);
        }
    }

    private Primitives() {}
}
