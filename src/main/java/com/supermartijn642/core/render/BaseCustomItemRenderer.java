package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public abstract class BaseCustomItemRenderer extends BlockEntityWithoutLevelRenderer {

    public BaseCustomItemRenderer(){
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        this.render(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
    }

    /**
     * Renders the given item stack.
     * {@link #renderDefaultModel(ItemStack, ItemTransforms.TransformType, PoseStack, MultiBufferSource, int, int)} may be used to render the item's baked model.
     */
    protected abstract void render(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);

    /**
     * Renders the baked model corresponding to the given item stack. Ignores any custom renderers associated with the item.
     */
    protected final void renderDefaultModel(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(itemStack.isEmpty())
            return;

        ItemRenderer itemRenderer = ClientUtils.getItemRenderer();
        BakedModel model = itemRenderer.getModel(itemStack, null, null, 0);

        boolean fabulous;
        if(transformType != ItemTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem){
            Block block = ((BlockItem)itemStack.getItem()).getBlock();
            fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        }else
            fabulous = true;

        for(var passModel : model.getRenderPasses(itemStack, fabulous)){
            for(var renderType : passModel.getRenderTypes(itemStack, fabulous)){
                VertexConsumer vertexConsumer = fabulous ?
                    ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, itemStack.hasFoil()) :
                    ItemRenderer.getFoilBuffer(bufferSource, renderType, true, itemStack.hasFoil());
                itemRenderer.renderModelLists(passModel, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
            }
        }
    }
}
