package com.example.cosmetics.gui;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.example.cosmetics.gui.widgets.Slider;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

/** Settings panel for a single feature. Opens on right-click in MainMenuScreen. */
public class SettingsScreen extends Screen {

    private final FeatureType feature;
    private final FeatureSettings fs;
    private final List<Slider> sliders = new ArrayList<>();
    private long openedAtMs;

    public SettingsScreen(FeatureType feature) {
        super(new StringTextComponent("Settings: " + feature.displayName));
        this.feature = feature;
        this.fs = CosmeticsState.get().settings(feature);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        this.openedAtMs = System.currentTimeMillis();
        sliders.clear();

        int panelW = 300;
        int panelH = 260;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        int sx = px + 12;
        int sy = py + 40;
        int sw = panelW - 24;
        int rowH = 22;
        int i = 0;

        if (feature.has(FeatureType.Caps.COLOR)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Red",   0F, 1F,
                    () -> fs.colorR, v -> fs.colorR = v));
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Green", 0F, 1F,
                    () -> fs.colorG, v -> fs.colorG = v));
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Blue",  0F, 1F,
                    () -> fs.colorB, v -> fs.colorB = v));
        }
        if (feature.has(FeatureType.Caps.SIZE)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Size", 0.25F, 3F,
                    () -> fs.size, v -> fs.size = v));
        }
        if (feature.has(FeatureType.Caps.DENSITY)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Density", 0F, 3F,
                    () -> fs.density, v -> fs.density = v));
        }
        if (feature.has(FeatureType.Caps.SPEED)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Speed", 0.25F, 3F,
                    () -> fs.speed, v -> fs.speed = v));
        }
        if (feature.has(FeatureType.Caps.COUNT)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Count", 1F, 30F,
                    () -> fs.count, v -> fs.count = (int) (float) v));
        }
        if (feature.has(FeatureType.Caps.STYLE)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Style", 0F, 3F,
                    () -> fs.style, v -> fs.style = (int) (float) v));
        }
        if (feature.has(FeatureType.Caps.OFFSET)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Offset X", -2F, 2F,
                    () -> fs.offsetX, v -> fs.offsetX = v));
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Offset Y", -2F, 2F,
                    () -> fs.offsetY, v -> fs.offsetY = v));
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Offset Z", -2F, 2F,
                    () -> fs.offsetZ, v -> fs.offsetZ = v));
        }
        if (feature.has(FeatureType.Caps.ROTATION)) {
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Rot X", -180F, 180F,
                    () -> fs.rotX, v -> fs.rotX = v));
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Rot Y", -180F, 180F,
                    () -> fs.rotY, v -> fs.rotY = v));
            sliders.add(new Slider(sx, sy + (i++) * rowH, sw, 16, "Rot Z", -180F, 180F,
                    () -> fs.rotZ, v -> fs.rotZ = v));
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        float anim = animProgress();
        fill(ms, 0, 0, this.width, this.height, (int)(anim * 140) << 24);

        int panelW = 300;
        int panelH = 260;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        GuiDraw.roundedPanel(ms, px, py, panelW, panelH, anim);

        int titleCol = ((int)(Math.max(0, Math.min(255, anim * 255))) << 24) | 0xFFFFFF;
        drawCenteredString(ms, this.font, this.title, px + panelW / 2, py + 14, titleCol);
        fill(ms, px + panelW / 2 - 60, py + 28, px + panelW / 2 + 60, py + 30,
                ((int)(anim * 255) << 24) | 0x8A5CFF);

        for (Slider s : sliders) s.draw(ms, anim);

        drawString(ms, this.font, "Esc to close", px + 10, py + panelH - 14, titleCol);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (Slider s : sliders) if (s.mousePressed(mx, my, button)) return true;
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        for (Slider s : sliders) s.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (Slider s : sliders) s.mouseDragged(mx);
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256 || key == 344) { // ESC or Right Shift
            Minecraft.getInstance().setScreen(new MainMenuScreen());
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    private float animProgress() {
        long t = System.currentTimeMillis() - openedAtMs;
        float p = Math.min(1F, t / 200F);
        return 1F - (1F - p) * (1F - p);
    }
}
