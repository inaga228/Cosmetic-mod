package com.example.cosmetics.particles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Base for client-only custom particles rendered as line-based 3D geometry
 * in {@code RenderWorldLastEvent}. Subclasses override {@link #renderShape}.
 */
public abstract class CustomParticle {
    public double x, y, z;
    public double vx, vy, vz;
    public int age = 0;
    public int maxAge = 25;
    public float size = 0.3F;
    public float rotation = 0.0F;
    public float rotationSpeed = 0.0F;
    public int r = 255, g = 255, b = 255;
    public boolean dead = false;

    public CustomParticle(double x, double y, double z, double vx, double vy, double vz) {
        this.x = x; this.y = y; this.z = z;
        this.vx = vx; this.vy = vy; this.vz = vz;
    }

    public void tick() {
        age++;
        if (age >= maxAge) { dead = true; return; }
        x += vx; y += vy; z += vz;
        vx *= 0.95; vy = vy * 0.97 + gravity(); vz *= 0.95;
        rotation += rotationSpeed;
    }

    /** Override to add gravity (e.g. snowflake falls, fire rises). */
    protected double gravity() { return 0.0; }

    public int alpha() {
        float life = 1.0F - (float) age / (float) maxAge;
        return Math.max(0, Math.min(255, (int) (life * 255)));
    }

    public final void render(MatrixStack ms, IRenderTypeBuffer buf, float partialTicks) {
        ms.pushPose();
        if (rotation != 0.0F) {
            ms.mulPose(Vector3f.YP.rotationDegrees(rotation));
        }
        renderShape(ms, buf, alpha());
        ms.popPose();
    }

    protected abstract void renderShape(MatrixStack ms, IRenderTypeBuffer buf, int alpha);
}
