package com.example.cosmetics.hud;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.gui.GuiDraw;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

/**
 * HUD that appears when the crosshair targets a mob/player. Shows:
 * - 2D head icon (skin for players, otherwise a default)
 * - display name
 * - smoothly animated HP bar
 * - a red flash when the target is hit
 * - fade in / fade out
 */
public final class TargetHud {

    private static LivingEntity lastTarget;
    private static long lastSeenMs;
    private static float shownAlpha = 0.0F;
    private static float shownHp = 0.0F;
    private static float flash = 0.0F;

    private static final long FADE_OUT_MS = 1200L;
    private static final ResourceLocation FALLBACK_SKIN = new ResourceLocation("minecraft", "textures/entity/steve.png");

    public static void onLivingHurt(LivingEntity ent) {
        if (ent == lastTarget) flash = 1.0F;
    }

    public static void render(MatrixStack ms, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        CosmeticsState state = CosmeticsState.get();
        if (!state.isOn(FeatureType.TARGET_HUD)) return;
        if (mc.player == null) return;

        LivingEntity target = resolveTarget();
        long now = System.currentTimeMillis();
        if (target != null) {
            lastTarget = target;
            lastSeenMs = now;
        }

        // Fade based on whether we have a live target.
        boolean visible = target != null || (lastTarget != null && (now - lastSeenMs) < FADE_OUT_MS);
        float targetAlpha = visible ? 1.0F : 0.0F;
        shownAlpha += (targetAlpha - shownAlpha) * 0.12F;
        if (shownAlpha < 0.01F || lastTarget == null) return;

        LivingEntity draw = target != null ? target : lastTarget;

        // Smoothly lerp HP
        float realHp = draw.getHealth() / Math.max(0.001F, draw.getMaxHealth());
        shownHp += (realHp - shownHp) * 0.12F;
        flash = Math.max(0.0F, flash - 0.05F);

        // Layout
        int w = 150;
        int h = 40;
        int x = (mc.getWindow().getGuiScaledWidth() - w) / 2;
        int y = 8;

        GuiDraw.roundedPanel(ms, x, y, w, h, shownAlpha);

        // Head icon 24x24
        int headX = x + 6;
        int headY = y + (h - 24) / 2;
        drawHead(ms, draw, headX, headY, 24, shownAlpha);

        // Name
        int textCol = ((int)(Math.max(0,Math.min(255, shownAlpha * 255))) << 24) | 0xFFFFFF;
        String name = draw.getDisplayName().getString();
        mc.font.drawShadow(ms, name, headX + 28, y + 6, textCol);

        // HP text
        String hpText = String.format("%.0f / %.0f", draw.getHealth(), draw.getMaxHealth());
        mc.font.drawShadow(ms, hpText, headX + 28, y + h - 16, textCol);

        // HP bar
        int barX = headX + 28;
        int barY = y + h / 2;
        int barW = w - (barX - x) - 8;
        int barH = 4;
        int bg = ((int)(shownAlpha * 140) << 24) | 0x222222;
        AbstractGui.fill(ms, barX, barY, barX + barW, barY + barH, bg);
        float fillPct = Math.max(0, Math.min(1, shownHp));
        int fillW = (int) (barW * fillPct);
        int r = flash > 0 ? 255 : 200;
        int g = flash > 0 ? (int)(60 + (1-flash) * 180) : 40;
        int bCol = flash > 0 ? (int)(60 + (1-flash) * 180) : 40;
        int hpCol = ((int)(shownAlpha * 255) << 24) | (r << 16) | (g << 8) | bCol;
        AbstractGui.fill(ms, barX, barY, barX + fillW, barY + barH, hpCol);
    }

    private static void drawHead(MatrixStack ms, LivingEntity ent, int x, int y, int size, float alpha) {
        ResourceLocation tex = FALLBACK_SKIN;
        if (ent instanceof AbstractClientPlayerEntity) {
            tex = ((AbstractClientPlayerEntity) ent).getSkinTextureLocation();
        }
        Minecraft.getInstance().getTextureManager().bind(tex);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1, 1, 1, alpha);
        // Head: u=8,v=8 size=8; overlay hat: u=40,v=8 size=8 (standard skin mapping)
        AbstractGui.blit(ms, x, y, size, size, 8F, 8F, 8, 8, 64, 64);
        AbstractGui.blit(ms, x, y, size, size, 40F, 8F, 8, 8, 64, 64);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private static LivingEntity resolveTarget() {
        Minecraft mc = Minecraft.getInstance();
        RayTraceResult rt = mc.hitResult;
        if (rt == null || rt.getType() != RayTraceResult.Type.ENTITY) return null;
        Entity e = ((EntityRayTraceResult) rt).getEntity();
        if (!(e instanceof LivingEntity)) return null;
        return (LivingEntity) e;
    }

    private TargetHud() {}
}
