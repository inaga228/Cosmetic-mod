package com.example.cosmetics.particles.shapes;

import com.example.cosmetics.particles.CustomParticle;
import com.example.cosmetics.render.Primitives;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class StarParticle extends CustomParticle {
    public StarParticle(double x, double y, double z, double vx, double vy, double vz) {
        super(x, y, z, vx, vy, vz);
        this.rotationSpeed = 3.0F;
        this.maxAge = 50;
    }

    @Override
    protected void renderShape(MatrixStack ms, IRenderTypeBuffer buf, int alpha) {
        IVertexBuilder vb = Primitives.lineBuffer(buf);
        Primitives.star(ms, vb, size * 0.5F, r, g, b, alpha);
    }
}
