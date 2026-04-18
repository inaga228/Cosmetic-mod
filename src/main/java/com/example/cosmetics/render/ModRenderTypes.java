package com.example.cosmetics.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Custom render types. Provides static factory methods for creating
 * specialized render types.
 */
public class ModRenderTypes {

    /** Untextured translucent colored quads (for the hat). */
    public static final RenderType COLOR_QUADS = RenderType.create(
            "cosmeticsmod_color_quads",
            DefaultVertexFormats.POSITION_COLOR,
            GL11.GL_QUADS, 256, false, true,
            RenderType.State.builder()
                    .setTransparencyState(new RenderState.TransparencyState("translucent_transparency", () -> {
                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                        com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(
                            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                            GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    }, () -> {
                        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                    }))
                    .setShadeModelState(new RenderState.ShadeModelState(true))
                    .setCullState(new RenderState.CullState(false))
                    .setLightmapState(new RenderState.LightmapState(false))
                    .setWriteMaskState(new RenderState.WriteMaskState(true, false))
                    .createCompositeState(false)
    );

    private ModRenderTypes() {}
}