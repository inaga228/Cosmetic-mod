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
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setShadeModelState(new RenderState.ShadeModelState(true))
                    .setCullState(RenderType.NO_CULL)
                    .setLightmapState(RenderType.NO_LIGHTMAP)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private ModRenderTypes() {}
}