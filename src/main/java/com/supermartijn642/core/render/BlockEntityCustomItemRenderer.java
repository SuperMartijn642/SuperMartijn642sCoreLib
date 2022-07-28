package com.supermartijn642.core.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BlockEntityCustomItemRenderer<T extends TileEntity> implements CustomItemRenderer {

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
    public void render(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        if(this.blockEntity == null){
            this.blockEntity = this.initEntity.get();
            if(this.blockEntity == null)
                throw new RuntimeException("Init entity function must not return a null block entity!");
        }
        this.entityUpdater.accept(itemStack, this.blockEntity);

        if(this.renderItemModel)
            this.renderDefaultModel(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
        TileEntityRendererDispatcher.instance.renderItem(this.blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
    }

    /**
     * Renders the baked model corresponding to the given item stack. Ignores any custom renderers associated with the item.
     */
    protected void renderDefaultModel(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        if(itemStack.isEmpty())
            return;

        ItemRenderer itemRenderer = ClientUtils.getItemRenderer();
        IBakedModel model = itemRenderer.getModel(itemStack, null, null);

        boolean fabulous;
        if(transformType != ItemCameraTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem){
            Block block = ((BlockItem)itemStack.getItem()).getBlock();
            fabulous = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
        }else
            fabulous = true;

        if(model.isLayered())
            ForgeHooksClient.drawItemLayered(itemRenderer, model, itemStack, poseStack, bufferSource, combinedLight, combinedOverlay, fabulous);
        else{
            RenderType renderType = RenderTypeLookup.getRenderType(itemStack, fabulous);
            IVertexBuilder vertexConsumer = fabulous ?
                ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, itemStack.hasFoil()) :
                ItemRenderer.getFoilBuffer(bufferSource, renderType, true, itemStack.hasFoil());
            itemRenderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
        }
    }
}
