package com.example.cosmetics.trails;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.particles.CustomParticle;
import com.example.cosmetics.particles.ParticleManager;
import com.example.cosmetics.particles.shapes.CubeParticle;
import com.example.cosmetics.particles.shapes.StarParticle;
import com.example.cosmetics.particles.shapes.TetraParticle;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;

/**
 * Spawns trail particles at the local player's feet each tick.
 */
public final class TrailTicker {
    private static final Random RNG = new Random();
    private static float hue = 0.0F;

    public static void tick(PlayerEntity player) {
        if (player == null) return;
        CosmeticsState s = CosmeticsState.get();

        double x = player.getX();
        double y = player.getY() + 0.15;
        double z = player.getZ();

        if (s.isOn(FeatureType.RAINBOW_TRAIL)) spawnRainbow(s.settings(FeatureType.RAINBOW_TRAIL), x, y, z);
        if (s.isOn(FeatureType.FLAME_TRAIL))   spawnFlame(s.settings(FeatureType.FLAME_TRAIL), x, y, z);
        if (s.isOn(FeatureType.GALAXY_TRAIL))  spawnGalaxy(s.settings(FeatureType.GALAXY_TRAIL), x, y, z);
    }

    private static void spawnRainbow(FeatureSettings fs, double x, double y, double z) {
        int count = Math.max(1, (int) Math.round(2 * fs.density));
        for (int i = 0; i < count; i++) {
            hue = (hue + 0.03F) % 1.0F;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
            CubeParticle p = new CubeParticle(
                    x + (RNG.nextDouble() - 0.5) * 0.2,
                    y,
                    z + (RNG.nextDouble() - 0.5) * 0.2,
                    (RNG.nextDouble() - 0.5) * 0.03 * fs.speed,
                    0.02 * fs.speed,
                    (RNG.nextDouble() - 0.5) * 0.03 * fs.speed);
            p.r = (rgb >> 16) & 0xFF;
            p.g = (rgb >> 8) & 0xFF;
            p.b = rgb & 0xFF;
            p.size = 0.25F * fs.size;
            ParticleManager.get().add(p);
        }
    }

    private static void spawnFlame(FeatureSettings fs, double x, double y, double z) {
        int count = Math.max(1, (int) Math.round(2 * fs.density));
        for (int i = 0; i < count; i++) {
            TetraParticle p = new TetraParticle(
                    x + (RNG.nextDouble() - 0.5) * 0.15,
                    y,
                    z + (RNG.nextDouble() - 0.5) * 0.15,
                    (RNG.nextDouble() - 0.5) * 0.02 * fs.speed,
                    0.04 * fs.speed,
                    (RNG.nextDouble() - 0.5) * 0.02 * fs.speed);
            applyColor(p, fs);
            p.size = 0.35F * fs.size;
            ParticleManager.get().add(p);
        }
    }

    private static void spawnGalaxy(FeatureSettings fs, double x, double y, double z) {
        int count = Math.max(1, (int) Math.round(1.5 * fs.density));
        for (int i = 0; i < count; i++) {
            StarParticle p = new StarParticle(
                    x + (RNG.nextDouble() - 0.5) * 0.25,
                    y + 0.2 + RNG.nextDouble() * 0.5,
                    z + (RNG.nextDouble() - 0.5) * 0.25,
                    (RNG.nextDouble() - 0.5) * 0.04 * fs.speed,
                    (RNG.nextDouble() - 0.5) * 0.02 * fs.speed,
                    (RNG.nextDouble() - 0.5) * 0.04 * fs.speed);
            applyColor(p, fs);
            p.size = 0.25F * fs.size;
            ParticleManager.get().add(p);
        }
    }

    private static void applyColor(CustomParticle p, FeatureSettings fs) {
        p.r = (int) (fs.colorR * 255);
        p.g = (int) (fs.colorG * 255);
        p.b = (int) (fs.colorB * 255);
    }

    private TrailTicker() {}
}
