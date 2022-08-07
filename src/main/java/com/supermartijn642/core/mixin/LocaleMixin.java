package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.LanguageLoader;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created 07/08/2022 by SuperMartijn642
 */
@Mixin(Locale.class)
public class LocaleMixin {

    @Redirect(
        method = "loadLocaleDataFiles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/IResourceManager;getAllResources(Lnet/minecraft/util/ResourceLocation;)Ljava/util/List;"
        )
    )
    private List<IResource> loadLocaleDataFilesRedirect(IResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException{
        return LanguageLoader.findAllResources(resourceManager, resourceLocation);
    }

    @Redirect(
        method = "loadLocaleData(Ljava/util/List;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/IResource;getInputStream()Ljava/io/InputStream;"
        )
    )
    private InputStream loadLocaleDataRedirect(IResource resource){
        if(resource.getResourceLocation().getResourcePath().endsWith(".json")){
            LanguageLoader.loadLanguageJson(((Locale)(Object)this).properties, resource);
            return null;
        }
        return resource.getInputStream();
    }

    @Inject(
        method = "loadLocaleData(Ljava/io/InputStream;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void loadLocaleData(InputStream inputStream, CallbackInfo ci){
        if(inputStream == null)
            ci.cancel();
    }
}
