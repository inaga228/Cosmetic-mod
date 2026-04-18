package com.example.cosmetics.particles.shapes;

import com.example.cosmetics.particles.CustomParticle;
import com.example.cosmetics.render.Primitives;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;

/** Golden spark for crit hit effect: small burst star that drops slightly. */
public class CritParticle extends CustomParticle {
    public CritParticle(double x, double y, double z, double vx, double vy, double vz) {
        super(x, y, z, vx, vy, vz);
        this.rotationSpeed = 12.0F;
        this.maxAge = 20;
    }

    @Override protected double gravity() { return -0.03; }

    @Override
    protected void renderShape(MatrixStack ms, IRenderTypeBuffer buf, int alpha) {
        IVertexBuilder vb = Primitives.lineBuffer(buf);
        Primitives.star(ms, vb, size * 0.35F, r, g, b, alpha);
    }
}
