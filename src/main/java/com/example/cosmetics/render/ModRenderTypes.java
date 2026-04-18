package com.example.cosmetics.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Custom render types. Subclassing RenderType so we can access the
 * protected {@code State.Builder} / {@code create(...)} APIs in 1.16.5.
 */
public class ModRenderTypes extends RenderType {

    /** Untextured translucent colored quads (for the hat). */
    public static final RenderType COLOR_QUADS = create(
            "cosmeticsmod_color_quads",
            DefaultVertexFormats.POSITION_COLOR,
            GL11.GL_QUADS, 256, false, true,
            RenderType.State.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setShadeModelState(new RenderState.ShadeModelState(true))
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );
}
