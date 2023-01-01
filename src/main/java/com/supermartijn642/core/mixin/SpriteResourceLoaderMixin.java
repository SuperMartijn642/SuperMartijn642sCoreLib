package com.supermartijn642.core.mixin;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * Created 01/01/2023 by SuperMartijn642
 */
@Mixin(SpriteResourceLoader.class)
public class SpriteResourceLoaderMixin {

    @ModifyVariable(
        method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;",
        at = @At("STORE"),
        ordinal = 0
    )
    public List<SpriteSource> appendSprites(List<SpriteSource> sprites, ResourceManager resourceManager, ResourceLocation atlas){
        return sprites;
    }
}
