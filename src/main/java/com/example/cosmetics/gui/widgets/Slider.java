package com.example.cosmetics.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/** A simple horizontal slider widget with a label. */
public class Slider {
    public final int x, y, w, h;
    public final String label;
    public final float min, max;
    private final DoubleSupplier getter;
    private final Consumer<Float> setter;
    private boolean dragging = false;

    public Slider(int x, int y, int w, int h, String label, float min, float max,
                  DoubleSupplier getter, Consumer<Float> setter) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.label = label; this.min = min; this.max = max;
        this.getter = getter; this.setter = setter;
    }

    public boolean contains(double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public boolean mousePressed(double mx, double my, int button) {
        if (button == 0 && contains(mx, my)) { dragging = true; updateFromMouse(mx); return true; }
        return false;
    }

    public void mouseReleased() { dragging = false; }

    public void mouseDragged(double mx) { if (dragging) updateFromMouse(mx); }

    private void updateFromMouse(double mx) {
        double t = Math.max(0, Math.min(1, (mx - x) / (double) w));
        setter.accept((float) (min + t * (max - min)));
    }

    public void draw(MatrixStack ms, float alpha) {
        int a = clamp((int) (alpha * 255));
        AbstractGui.fill(ms, x, y, x + w, y + h, (a << 24) | 0x1E1B2E);
        float t = (float) ((getter.getAsDouble() - min) / (max - min));
        t = Math.max(0, Math.min(1, t));
        int fillW = (int) (w * t);
        AbstractGui.fill(ms, x, y, x + fillW, y + h, (a << 24) | 0x7A4CFF);
        int knobX = x + fillW - 2;
        AbstractGui.fill(ms, knobX, y - 1, knobX + 4, y + h + 1, (a << 24) | 0xFFFFFF);

        String text = String.format("%s: %.2f", label, getter.getAsDouble());
        Minecraft.getInstance().font.drawShadow(ms, text, x + 4, y + (h - 8) / 2, (a << 24) | 0xFFFFFF);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
