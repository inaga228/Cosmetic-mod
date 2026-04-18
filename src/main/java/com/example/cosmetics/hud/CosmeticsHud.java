package com.example.cosmetics.hud;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.gui.GuiDraw;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class CosmeticsHud {
    private static float shownAlpha = 0.0F;

    public static void render(MatrixStack ms, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        CosmeticsState s = CosmeticsState.get();
        boolean want = s.isOn(FeatureType.COSMETICS_HUD);
        float target = want ? 1.0F : 0.0F;
        shownAlpha += (target - shownAlpha) * 0.15F;
        if (shownAlpha < 0.01F) return;

        PlayerEntity p = mc.player;
        FontRenderer f = mc.font;

        List<String> lines = new ArrayList<>();
        for (FeatureType ft : s.active()) {
            if (ft == FeatureType.COSMETICS_HUD) continue;
            lines.add("- " + ft.displayName);
        }
        if (lines.isEmpty()) lines.add("No effects active");

        int padding = 6;
        int lineH = 10;
        int w = Math.max(120, f.width(p.getGameProfile().getName()) + 40);
        for (String l : lines) w = Math.max(w, f.width(l) + padding * 2 + 6);
        int h = padding + 12 + 4 + lines.size() * lineH + padding;

        int x = 8, y = 8;
        GuiDraw.roundedPanel(ms, x, y, w, h, shownAlpha);

        int a = Math.max(0, Math.min(255, (int) (shownAlpha * 255)));
        int nameCol = (a << 24) | 0xFFFFFF;
        int subCol  = (a << 24) | 0xB8A8FF;

        f.drawShadow(ms, p.getGameProfile().getName(), x + padding, y + padding, nameCol);
        int divCol = (Math.max(0, Math.min(255, (int)(shownAlpha * 120))) << 24) | 0x8A5CFF;
        AbstractGui.fill(ms, x + padding, y + padding + 10, x + w - padding, y + padding + 11, divCol);

        int ly = y + padding + 14;
        for (String l : lines) {
            f.drawShadow(ms, l, x + padding, ly, subCol);
            ly += lineH;
        }
    }

    private CosmeticsHud() {}
}
