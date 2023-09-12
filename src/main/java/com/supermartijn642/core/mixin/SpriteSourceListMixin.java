package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Optional;

/**
 * Created 01/01/2023 by SuperMartijn642
 */
@Mixin(SpriteSourceList.class)
public class SpriteSourceListMixin {

    @ModifyVariable(
        method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/atlas/SpriteSourceList;",
        at = @At("STORE"),
        ordinal = 0
    )
    private static List<SpriteSource> appendSprites(List<SpriteSource> sprites, ResourceManager resourceManager, ResourceLocation atlas){
        ClientRegistrationHandler.collectSprites(atlas, location -> sprites.add(new SingleFile(location, Optional.empty())));
        return sprites;
    }
}
