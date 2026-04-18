package com.example.cosmetics.render;

import com.example.cosmetics.client.CosmeticsState;
import com.example.cosmetics.feature.FeatureSettings;
import com.example.cosmetics.feature.FeatureType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Draws a procedural cone-like "China Hat" above the local player, rendered
 * in {@code RenderWorldLastEvent}. Key traits:
 *
 * <ul>
 *     <li>Only the local player (no hats on others)</li>
 *     <li>Follows player Y rotation only (yaw); never pitches</li>
 *     <li>Translucent (alpha < 255) colored quads</li>
 *     <li>Color, size, style and XYZ offset come from {@link FeatureSettings}</li>
 * </ul>
 *
 * Style values: 0 = cone, 1 = flat disc, 2 = wide cone.
 */
public final class HatRenderer {

    public static void render(MatrixStack ms, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        CosmeticsState state = CosmeticsState.get();
        if (!state.isOn(FeatureType.CHINA_HAT)) return;
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        FeatureSettings fs = state.settings(FeatureType.CHINA_HAT);

        // Interpolate player position for smooth movement.
        double x = player.xo + (player.getX() - player.xo) * partialTicks;
        double y = player.yo + (player.getY() - player.yo) * partialTicks;
        double z = player.zo + (player.getZ() - player.zo) * partialTicks;

        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();

        // Head top is roughly player.getY() + player.eye height. Keep above head.
        double eye = player.getEyeHeight();
        double dx = x - cam.x + fs.offsetX;
        double dy = y - cam.y + eye + 0.35 + fs.offsetY;
        double dz = z - cam.z + fs.offsetZ;

        // Yaw only (no pitch/roll): interpolate yBodyRot for body, or yRot for head yaw.
        float yaw = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO) * partialTicks;

        ms.pushPose();
        ms.translate(dx, dy, dz);
        ms.mulPose(Vector3f.YP.rotationDegrees(-yaw));

        int style = Math.floorMod(fs.style, 3);
        float size = Math.max(0.1F, fs.size);

        int r = (int) (fs.colorR * 255);
        int g = (int) (fs.colorG * 255);
        int b = (int) (fs.colorB * 255);
        int a = 160;  // translucent

        IRenderTypeBuffer.Impl buf = mc.renderBuffers().bufferSource();
        IVertexBuilder vb = buf.getBuffer(ModRenderTypes.COLOR_QUADS);

        float radius, height;
        switch (style) {
            case 1: radius = 0.55F * size; height = 0.12F * size; break; // flat disc
            case 2: radius = 0.9F * size;  height = 0.35F * size; break; // wide cone
            case 0:
            default: radius = 0.55F * size; height = 0.45F * size; break; // cone
        }
        drawCone(ms, vb, radius, height, r, g, b, a);

        ms.popPose();
        buf.endBatch(ModRenderTypes.COLOR_QUADS);
    }

    private static void drawCone(MatrixStack ms, IVertexBuilder vb,
                                 float radius, float height,
                                 int r, int g, int b, int a) {
        int sides = 20;
        Matrix4f pose = ms.last().pose();

        for (int i = 0; i < sides; i++) {
            float a0 = (float) (i       * (Math.PI * 2 / sides));
            float a1 = (float) ((i + 1) * (Math.PI * 2 / sides));
            float x0 = (float) Math.cos(a0) * radius;
            float z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;

            // Side quad (apex duplicated to make a triangle as a degenerate quad).
            vb.vertex(pose, 0.0F, height, 0.0F).color(r, g, b, a).endVertex();
            vb.vertex(pose, x0,   0.0F,   z0).color(r, g, b, a).endVertex();
            vb.vertex(pose, x1,   0.0F,   z1).color(r, g, b, a).endVertex();
            vb.vertex(pose, 0.0F, height, 0.0F).color(r, g, b, a).endVertex();

            // Bottom cap quad (toward center).
            vb.vertex(pose, 0.0F, 0.0F, 0.0F).color(r, g, b, a).endVertex();
            vb.vertex(pose, x1,   0.0F, z1  ).color(r, g, b, a).endVertex();
            vb.vertex(pose, x0,   0.0F, z0  ).color(r, g, b, a).endVertex();
            vb.vertex(pose, 0.0F, 0.0F, 0.0F).color(r, g, b, a).endVertex();
        }
    }

    private HatRenderer() {}
}
