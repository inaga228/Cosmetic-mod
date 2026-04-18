package com.example.cosmetics.gui;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Main cosmetics menu: left side shows categories, right side shows feature
 * cards. Left-click toggles a feature, right-click opens its {@link SettingsScreen}.
 */
public class MainMenuScreen extends Screen {

    private FeatureType.Category current = FeatureType.Category.TRAILS;
    private long openedAtMs;
    private boolean closing = false;
    private long closingAtMs;
    private static final long ANIM_MS = 220L;

    private final List<CategoryTab> categoryTabs = new ArrayList<>();
    private final List<FeatureCard> cards = new ArrayList<>();

    public MainMenuScreen() { super(new StringTextComponent("Cosmetics")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        openedAtMs = System.currentTimeMillis();
        closing = false;
        categoryTabs.clear();

        int panelW = 360;
        int panelH = 220;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        int i = 0;
        for (FeatureType.Category c : FeatureType.Category.values()) {
            categoryTabs.add(new CategoryTab(px + 10, py + 40 + i * 24, 90, 20, c));
            i++;
        }
        rebuildCards(px, py);
    }

    private void rebuildCards(int px, int py) {
        cards.clear();
        int cx = px + 110;
        int cy = py + 40;
        int cw = 230;
        int ch = 22;
        int i = 0;
        for (FeatureType f : FeatureType.values()) {
            if (f.category != current) continue;
            cards.add(new FeatureCard(cx, cy + i * (ch + 4), cw, ch, f));
            i++;
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        float anim = animProgress();
        fill(ms, 0, 0, this.width, this.height, (int)(anim * 140) << 24);

        int panelW = 360;
        int panelH = 220;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        ms.pushPose();
        float scale = 0.9F + 0.1F * anim;
        ms.translate(this.width / 2f, this.height / 2f, 0);
        ms.scale(scale, scale, 1.0F);
        ms.translate(-this.width / 2f, -this.height / 2f, 0);

        GuiDraw.roundedPanel(ms, px, py, panelW, panelH, anim);

        int titleCol = ((int)(Math.max(0, Math.min(255, anim * 255))) << 24) | 0xFFFFFF;
        drawCenteredString(ms, this.font, "Cosmetics", px + panelW / 2, py + 14, titleCol);
        fill(ms, px + panelW / 2 - 50, py + 28, px + panelW / 2 + 50, py + 30,
                ((int)(anim * 255) << 24) | 0x8A5CFF);

        for (CategoryTab c : categoryTabs) c.draw(ms, mouseX, mouseY, anim, current);
        for (FeatureCard c : cards) c.draw(ms, mouseX, mouseY, anim);

        drawString(ms, this.font, "LMB toggle | RMB settings | Esc close",
                px + 10, py + panelH - 14,
                ((int)(anim * 255) << 24) | 0xAAAAAA);

        ms.popPose();

        if (closing && anim <= 0.001F) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (closing) return false;
        for (CategoryTab c : categoryTabs) {
            if (c.contains(mx, my)) {
                current = c.category;
                int panelW = 360, panelH = 220;
                int px = (this.width - panelW) / 2;
                int py = (this.height - panelH) / 2;
                rebuildCards(px, py);
                return true;
            }
        }
        for (FeatureCard c : cards) {
            if (c.contains(mx, my)) {
                if (button == 0) {
                    CosmeticsState.get().toggle(c.feature);
                } else if (button == 1) {
                    Minecraft.getInstance().setScreen(new SettingsScreen(c.feature));
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256 || key == 344) { // ESC or Right Shift
            closing = true; closingAtMs = System.currentTimeMillis(); return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override public void onClose() {
        if (!closing) { closing = true; closingAtMs = System.currentTimeMillis(); }
        else super.onClose();
    }

    private float animProgress() {
        long now = System.currentTimeMillis();
        if (!closing) {
            float t = Math.min(1F, (now - openedAtMs) / (float) ANIM_MS);
            return 1F - (1F - t) * (1F - t);
        } else {
            float t = Math.min(1F, (now - closingAtMs) / (float) ANIM_MS);
            return 1F - (1F - (1F - (1F - t) * (1F - t)));
        }
    }

    // ---- Widgets ------------------------------------------------------------

    private class CategoryTab {
        final int x, y, w, h;
        final FeatureType.Category category;
        CategoryTab(int x, int y, int w, int h, FeatureType.Category c) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.category = c;
        }
        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
        void draw(MatrixStack ms, int mx, int my, float alpha, FeatureType.Category selected) {
            boolean hover = contains(mx, my);
            boolean sel = selected == category;
            int base = sel ? 0xFF3A2D5E : (hover ? 0xFF2B2540 : 0xFF1E1B2E);
            fill(ms, x, y, x + w, y + h, withAlpha(base, alpha));
            if (sel) fill(ms, x, y, x + 3, y + h, withAlpha(0xFF8A5CFF, alpha));
            String label = category.name().charAt(0) + category.name().substring(1).toLowerCase();
            drawString(ms, font, label, x + 8, y + (h - 8) / 2, withAlpha(0xFFFFFFFF, alpha));
        }
    }

    private class FeatureCard {
        final int x, y, w, h;
        final FeatureType feature;
        FeatureCard(int x, int y, int w, int h, FeatureType f) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.feature = f;
        }
        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
        void draw(MatrixStack ms, int mx, int my, float alpha) {
            boolean hover = contains(mx, my);
            boolean on = CosmeticsState.get().isOn(feature);
            int base = hover ? 0xFF2B2540 : 0xFF1E1B2E;
            fill(ms, x, y, x + w, y + h, withAlpha(base, alpha));
            if (on) fill(ms, x, y, x + 3, y + h, withAlpha(0xFF7A4CFF, alpha));

            drawString(ms, font, feature.displayName, x + 10, y + (h - 8) / 2,
                    withAlpha(0xFFFFFFFF, alpha));

            int pillW = 24, pillH = 10;
            int pX = x + w - pillW - 8;
            int pY = y + (h - pillH) / 2;
            int pillBg = on ? 0xFF7A4CFF : 0xFF444050;
            fill(ms, pX, pY, pX + pillW, pY + pillH, withAlpha(pillBg, alpha));
            int knobX = on ? pX + pillW - pillH : pX;
            fill(ms, knobX, pY, knobX + pillH, pY + pillH, withAlpha(0xFFFFFFFF, alpha));
        }
    }

    private static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, (int) ((argb >>> 24 & 0xFF) * alpha)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
