package com.example.cosmetics.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Custom render types. Subclassing RenderType so we can access the
 * protected {@code State.Builder} / {@code create(...)} APIs in 1.16.5.
 */
public class ModRenderTypes extends RenderType {
    // Dummy constructor; the class is only used to access the protected helpers.
    private ModRenderTypes(String name, VertexFormat fmt, int mode, int bufSize,
                           boolean useDelegate, boolean needsSorting,
                           Runnable on, Runnable off) {
        super(name, fmt, mode, bufSize, useDelegate, needsSorting, on, off);
    }

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
