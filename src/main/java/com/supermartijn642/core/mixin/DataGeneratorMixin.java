package com.supermartijn642.core.mixin;

import com.google.common.base.Stopwatch;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.extensions.CoreLibDataGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.WorldVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
@Mixin(DataGenerator.class)
public class DataGeneratorMixin implements CoreLibDataGenerator {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private GeneratorRegistrationHandler handler;
    @Unique
    private ResourceCache resourceCache;
    @Shadow
    @Final
    private List<DataProvider> providersToRun;
    @Shadow
    @Final
    private boolean alwaysGenerate;
    @Shadow
    @Final
    private WorldVersion version;
    @Shadow
    @Final
    private List<DataProvider> allProviders;
    @Shadow
    @Final
    private Path outputFolder;

    @Inject(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/base/Stopwatch;createStarted()Lcom/google/common/base/Stopwatch;",
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void runHead(CallbackInfo ci, HashCache hashCache){
        //noinspection ConstantValue
        if(this.handler != null && (Object)this instanceof FabricDataGenerator){
            FabricDataGenerator dataGenerator = (FabricDataGenerator)(Object)this;
            // Get the output folder and manual files folder
            String manualFolderProperty = System.getProperty("fabric-api.datagen.manual-dir");
            Path outputFolder = dataGenerator.getOutputFolder(), manualFolder = manualFolderProperty == null || manualFolderProperty.isBlank() ? null : Paths.get(manualFolderProperty);
            if(manualFolder == null)
                CoreLib.LOGGER.warn("Property 'fabric-api.datagen.manual-dir' has not been set! Manually created files may not be recognised!");
            // Create a ResourceCache instance
            this.resourceCache = ResourceCache.wrap(hashCache, outputFolder, manualFolder);
            ((ResourceCache.HashCacheWrapper)this.resourceCache).allowWrites(false);
            this.handler.registerProviders(dataGenerator, this.resourceCache);
            // Add the new providers to the hash cache
            for(DataProvider provider : this.allProviders){
                Path cachePath = hashCache.getProviderCachePath(provider);
                hashCache.cachePaths.add(cachePath);
                HashCache.ProviderCache providerCache = HashCache.readCache(outputFolder, cachePath);
                hashCache.existingCaches.put(provider, providerCache);
                hashCache.initialCount += providerCache.count();
            }
        }
        if(this.resourceCache != null)
            ((ResourceCache.HashCacheWrapper)this.resourceCache).readHashCache();
    }

    @Inject(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/base/Stopwatch;createUnstarted()Lcom/google/common/base/Stopwatch;",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void runBeforeGenerators(CallbackInfo ci, HashCache hashCache){
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        for(DataProvider provider : this.providersToRun){
            if(!this.alwaysGenerate && !hashCache.shouldRunInThisVersion(provider)){
                LOGGER.debug("Generator {} already run for version {}", provider.getName(), this.version.getName());
                return;
            }
            if(provider instanceof ResourceGenerator.DataProviderInstance){
                LOGGER.info("Running generator: {}", provider.getName());
                stopwatch.start();
                ((ResourceGenerator.DataProviderInstance)provider).generate();
                stopwatch.stop();
                LOGGER.info("{} finished after {} ms", provider.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
                stopwatch.reset();
            }
        }
        if(this.resourceCache != null)
            ((ResourceCache.HashCacheWrapper)this.resourceCache).allowWrites(true);
    }

    @Inject(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/data/HashCache;purgeStaleAndWrite()V",
            shift = At.Shift.BEFORE
        )
    )
    private void runTail(CallbackInfo ci){
        if(this.resourceCache != null)
            ((ResourceCache.HashCacheWrapper)this.resourceCache).finish();
    }

    @Override
    public void setGeneratorRegistrationHandler(GeneratorRegistrationHandler handler){
        this.handler = handler;
    }

    @Override
    public GeneratorRegistrationHandler getGeneratorRegistrationHandler(){
        return this.handler;
    }
}
