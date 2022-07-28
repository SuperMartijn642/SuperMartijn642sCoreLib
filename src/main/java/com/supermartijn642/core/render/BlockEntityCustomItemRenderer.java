package com.supermartijn642.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BlockEntityCustomItemRenderer<T extends BlockEntity> implements CustomItemRenderer {

    protected final boolean renderItemModel;
    protected final Supplier<T> initEntity;
    protected final BiConsumer<ItemStack,T> entityUpdater;
    protected T blockEntity;

    public BlockEntityCustomItemRenderer(boolean renderItemModel, Supplier<T> initEntity, BiConsumer<ItemStack,T> entityUpdater){
        this.renderItemModel = renderItemModel;
        this.initEntity = initEntity;
        this.entityUpdater = entityUpdater;
    }

    @Override
    public void render(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(this.blockEntity == null){
            this.blockEntity = this.initEntity.get();
            if(this.blockEntity == null)
                throw new RuntimeException("Init entity function must not return a null block entity!");
        }
        this.entityUpdater.accept(itemStack, this.blockEntity);

        if(this.renderItemModel)
            this.renderDefaultModel(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
        ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().renderItem(this.blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
    }

    /**
     * Renders the baked model corresponding to the given item stack. Ignores any custom renderers associated with the item.
     */
    protected void renderDefaultModel(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
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

        RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, fabulous);
        VertexConsumer vertexConsumer = fabulous ?
            ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, itemStack.hasFoil()) :
            ItemRenderer.getFoilBuffer(bufferSource, renderType, true, itemStack.hasFoil());
        itemRenderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
    }
}