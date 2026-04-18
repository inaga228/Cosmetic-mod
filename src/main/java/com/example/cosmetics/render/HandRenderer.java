package com.example.cosmetics.render;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Applies View Model offset / rotation and custom swing animation transforms
 * to the first-person hand via {@code RenderHandEvent}. Called from
 * {@link com.example.cosmetics.client.ClientEvents#onRenderHand}.
 */
public final class HandRenderer {

    /** State shared with ClientEvents to drive the place animation. */
    public static int placeAnimTicks = 0;   // countdown ticks left for place anim

    public static void applyTransforms(MatrixStack ms, float partialTicks) {
        CosmeticsState s = CosmeticsState.get();

        if (s.isOn(FeatureType.VIEW_MODEL)) {
            FeatureSettings fs = s.settings(FeatureType.VIEW_MODEL);
            ms.translate(fs.offsetX * 0.5F, fs.offsetY * 0.5F, fs.offsetZ * 0.5F);
            if (fs.rotX != 0) ms.mulPose(Vector3f.XP.rotationDegrees(fs.rotX));
            if (fs.rotY != 0) ms.mulPose(Vector3f.YP.rotationDegrees(fs.rotY));
            if (fs.rotZ != 0) ms.mulPose(Vector3f.ZP.rotationDegrees(fs.rotZ));
        }

        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        if (s.isOn(FeatureType.CUSTOM_ATTACK)) {
            float swing = p.getAttackAnim(partialTicks);
            // Custom arc: lift up, then down hard, with a little roll.
            if (swing > 0.0F) {
                float strength = 1.0F; // could use settings.size
                float lift  = (float) Math.sin(swing * Math.PI) * 12.0F * strength;
                float roll  = (float) Math.sin(swing * Math.PI * 2) * 6.0F * strength;
                ms.mulPose(Vector3f.XP.rotationDegrees(-lift));
                ms.mulPose(Vector3f.ZP.rotationDegrees(roll));
            }
        }

        if (s.isOn(FeatureType.CUSTOM_PLACE) && placeAnimTicks > 0) {
            float t = placeAnimTicks / 8.0F;  // 8 ticks animation
            float thrust = (float) Math.sin(t * Math.PI) * 0.2F;
            ms.translate(0, -thrust, thrust);
            ms.mulPose(Vector3f.XP.rotationDegrees(t * 15.0F));
        }
    }

    public static void tickPlaceAnim() {
        if (placeAnimTicks > 0) placeAnimTicks--;
    }

    public static void triggerPlaceAnim() { placeAnimTicks = 8; }

    private HandRenderer() {}
}
