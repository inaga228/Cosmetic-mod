package com.example.cosmetics.hit;

import com.example.cosmetics.CosmeticsMod;
import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.particles.CustomParticle;
import com.example.cosmetics.particles.ParticleManager;
import com.example.cosmetics.particles.shapes.CritParticle;
import com.example.cosmetics.particles.shapes.SlashParticle;
import com.example.cosmetics.particles.shapes.StarParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * When the local player attacks another entity, spawn a burst of client-side
 * custom particles at the target's hit location based on the selected style.
 *
 * We subscribe to both {@link AttackEntityEvent} (fires client-side when the
 * local player swings) and {@link LivingHurtEvent} (usually server-side, but
 * handled defensively by checking world side) so the effect is responsive.
 */
@Mod.EventBusSubscriber(modid = CosmeticsMod.MOD_ID, value = Dist.CLIENT)
public final class HitEffectHandler {

    public enum Style { SLASH, STARS, CRIT }

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (Minecraft.getInstance().level == null) return;
        if (event.getPlayer() != Minecraft.getInstance().player) return;
        burst(event.getTarget());
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        // Only react client-side when the hurt entity is in the client world.
        LivingEntity ent = event.getEntityLiving();
        if (ent == null || ent.level == null || !ent.level.isClientSide) return;
        // Avoid double-bursting when AttackEntityEvent already fired from local player
        // by only triggering for remote-caused hurt:
        if (event.getSource() != null && event.getSource().getEntity() == Minecraft.getInstance().player) return;
        burst(ent);
    }

    private static void burst(Entity target) {
        if (target == null) return;
        CosmeticsState state = CosmeticsState.get();
        if (!state.isOn(FeatureType.HIT_EFFECT)) return;
        FeatureSettings fs = state.settings(FeatureType.HIT_EFFECT);

        Style style = Style.values()[Math.floorMod(fs.style, Style.values().length)];
        int count = Math.max(1, fs.count);

        double cx = target.getX();
        double cy = target.getY() + target.getBbHeight() * 0.6;
        double cz = target.getZ();

        for (int i = 0; i < count; i++) {
            double vx = (RNG.nextDouble() - 0.5) * 0.15;
            double vy = (RNG.nextDouble() - 0.5) * 0.15;
            double vz = (RNG.nextDouble() - 0.5) * 0.15;
            double px = cx + (RNG.nextDouble() - 0.5) * 0.4;
            double py = cy + (RNG.nextDouble() - 0.5) * 0.4;
            double pz = cz + (RNG.nextDouble() - 0.5) * 0.4;

            CustomParticle p;
            switch (style) {
                case SLASH: p = new SlashParticle(px, py, pz, vx, vy, vz); break;
                case STARS: p = new StarParticle(px, py, pz, vx, vy, vz); break;
                case CRIT:
                default:    p = new CritParticle(px, py, pz, vx, vy, vz); break;
            }

            // Defaults per style; user color overrides if set far from defaults.
            int[] rgb;
            switch (style) {
                case SLASH: rgb = new int[]{ 255, 40, 40 }; break;
                case STARS: rgb = new int[]{ 255, 240, 120 }; break;
                case CRIT:
                default:    rgb = new int[]{ 255, 200, 40 }; break;
            }
            if (fs.colorR != 1.0F || fs.colorG != 1.0F || fs.colorB != 1.0F) {
                rgb[0] = (int) (fs.colorR * 255);
                rgb[1] = (int) (fs.colorG * 255);
                rgb[2] = (int) (fs.colorB * 255);
            }
            p.r = rgb[0]; p.g = rgb[1]; p.b = rgb[2];
            p.size = 0.5F * fs.size;
            ParticleManager.get().add(p);
        }
    }

    private HitEffectHandler() {}
}
