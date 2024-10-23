package com.supermartijn642.core.render;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedOverrides;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created 25/07/2022 by SuperMartijn642
 */
public final class CustomRendererBakedModelWrapper implements BakedModel {

    public static BakedModel wrap(BakedModel originalModel){
        return new CustomRendererBakedModelWrapper(originalModel);
    }

    private final BakedModel originalModel;

    private CustomRendererBakedModelWrapper(BakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource random){
        return this.originalModel.getQuads(blockState, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion(){
        return this.originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight(){
        return this.originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer(){
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms(){
        return this.originalModel.getTransforms();
    }

    @Override
    public BakedOverrides overrides(){
        return this.originalModel.overrides();
    }

    @Override
    public boolean isVanillaAdapter(){
        return this.originalModel.isVanillaAdapter();
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context){
        this.originalModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context){
        ((FabricBakedModel)this.originalModel).emitItemQuads(stack, randomSupplier, context);
    }
}
