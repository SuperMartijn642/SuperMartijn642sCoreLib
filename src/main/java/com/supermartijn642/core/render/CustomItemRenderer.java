package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
public interface CustomItemRenderer<S> {

    static <T> SpecialModelRenderer<T> toSpecialModelRenderer(CustomItemRenderer<T> customRenderer){
        MutableCustomItemRendererContext context = new MutableCustomItemRendererContext();
        return new SpecialModelRenderer<>() {
            @Override
            public void submit(T state, PoseStack poseStack, SubmitNodeCollector output, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor){
                context.set(poseStack, lightCoords, overlayCoords);
                customRenderer.submit(output, state, hasFoil, context);
            }

            @Override
            public void getExtents(Consumer<Vector3fc> consumer){
                customRenderer.getExtents(consumer);
            }

            @Override
            public @Nullable T extractArgument(ItemStack stack){
                return customRenderer.extractState(stack);
            }
        };
    }

    @Nullable
    S extractState(ItemStack stack);

    void submit(SubmitNodeCollector output, S state, boolean hasFoil, RenderContext context);

    /**
     * Renders the given item stack.
     */
    void render(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);

    void getExtents(Consumer<Vector3fc> extents);

    interface RenderContext {
        ItemDisplayContext displayContext();

        PoseStack poseStack();

        int packedLight();

        int packedBreakingOverlay();
    }
}
