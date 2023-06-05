package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Created 05/06/2023 by SuperMartijn642
 */
public interface WidgetRenderContext {

    PoseStack poseStack();

    MultiBufferSource.BufferSource buffers();

    float partialTicks();
}
