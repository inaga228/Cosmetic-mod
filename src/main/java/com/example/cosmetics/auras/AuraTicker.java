package com.example.cosmetics.auras;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.particles.CustomParticle;
import com.example.cosmetics.particles.ParticleManager;
import com.example.cosmetics.particles.shapes.HeartParticle;
import com.example.cosmetics.particles.shapes.SnowflakeParticle;
import com.example.cosmetics.particles.shapes.SphereParticle;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;

public final class AuraTicker {
    private static final Random RNG = new Random();
    private static float angle = 0.0F;

    public static void tick(PlayerEntity player) {
        if (player == null) return;
        CosmeticsState s = CosmeticsState.get();

        // Advance the orbit. Tie speed to the fastest active aura's "speed" setting roughly.
        angle += 0.2F;
        if (angle > (float) (Math.PI * 2)) angle -= (float) (Math.PI * 2);

        if (s.isOn(FeatureType.AURA))       orbit(player, s.settings(FeatureType.AURA),      SphereParticle.class, 1.0, 1.2);
        if (s.isOn(FeatureType.SNOW_AURA))  rainDown(player, s.settings(FeatureType.SNOW_AURA));
        if (s.isOn(FeatureType.HEART_AURA)) orbit(player, s.settings(FeatureType.HEART_AURA), HeartParticle.class, 0.8, 1.7);
    }

    private static void orbit(PlayerEntity player, FeatureSettings fs, Class<?> type,
                              double radius, double yOff) {
        int count = Math.max(1, (int) Math.round(3 * fs.density));
        for (int i = 0; i < count; i++) {
            float a = angle + (float) (i * (Math.PI * 2 / count));
            double x = player.getX() + Math.cos(a) * radius;
            double z = player.getZ() + Math.sin(a) * radius;
            double y = player.getY() + yOff;
            CustomParticle p;
            if (type == HeartParticle.class) {
                p = new HeartParticle(x, y, z, 0, 0.01 * fs.speed, 0);
            } else {
                p = new SphereParticle(x, y, z, 0, 0.01 * fs.speed, 0);
            }
            applyColor(p, fs);
            p.size = 0.5F * fs.size;
            ParticleManager.get().add(p);
        }
    }

    private static void rainDown(PlayerEntity player, FeatureSettings fs) {
        int count = Math.max(1, (int) Math.round(2 * fs.density));
        for (int i = 0; i < count; i++) {
            double dx = (RNG.nextDouble() - 0.5) * 4;
            double dz = (RNG.nextDouble() - 0.5) * 4;
            double x = player.getX() + dx;
            double z = player.getZ() + dz;
            double y = player.getY() + 3.0;
            SnowflakeParticle p = new SnowflakeParticle(x, y, z, 0, -0.03 * fs.speed, 0);
            applyColor(p, fs);
            p.size = 0.5F * fs.size;
            ParticleManager.get().add(p);
        }
    }

    private static void applyColor(CustomParticle p, FeatureSettings fs) {
        p.r = (int) (fs.colorR * 255);
        p.g = (int) (fs.colorG * 255);
        p.b = (int) (fs.colorB * 255);
    }

    private AuraTicker() {}
}
