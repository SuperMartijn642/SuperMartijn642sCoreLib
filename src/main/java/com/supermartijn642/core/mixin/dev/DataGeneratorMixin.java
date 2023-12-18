package com.supermartijn642.core.mixin.dev;

import com.google.common.base.Stopwatch;
import com.supermartijn642.core.extensions.DataGeneratorExtension;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import net.minecraft.WorldVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.neoforged.neoforge.data.event.GatherDataEvent;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
@Mixin(DataGenerator.class)
public class DataGeneratorMixin implements DataGeneratorExtension {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private GatherDataEvent.DataGeneratorConfig config;
    @Unique
    private ResourceCache resourceCache;
    @Shadow
    @Final
    private Map<String,DataProvider> providersToRun;
    @Shadow
    @Final
    private boolean alwaysGenerate;
    @Shadow
    @Final
    private WorldVersion version;
    @Shadow
    @Final
    private Set<String> allProviderIds;

    @Override
    public void setDataGeneratorConfig(GatherDataEvent.DataGeneratorConfig config){
        this.config = config;
    }

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
        GatherDataEvent.DataGeneratorConfig dataGeneratorConfig = this.config;
        if(dataGeneratorConfig != null){ // Some mods run data generators themselves
            dataGeneratorConfig.getMods().stream().filter(GeneratorRegistrationHandler::hasHandlerForModid).forEach(modid -> {
                GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(modid);
                DataGenerator dataGenerator = (DataGenerator)(Object)this;
                // Get the output folder
                Path outputFolder = dataGenerator.rootOutputFolder;
                // Create a ResourceCache instance
                if(this.resourceCache == null)
                    this.resourceCache = ResourceCache.wrap(DatagenModLoaderAccessor.getExistingFileHelper(), hashCache, outputFolder);
                ((ResourceCache.HashCacheWrapper)this.resourceCache).allowWrites(false);
                handler.registerProviders(dataGenerator, DatagenModLoaderAccessor.getExistingFileHelper(), this.resourceCache);
                // Add the new providers to the hash cache
                for(String provider : this.allProviderIds){
                    Path cachePath = hashCache.getProviderCachePath(provider);
                    hashCache.cachePaths.add(cachePath);
                    HashCache.ProviderCache providerCache = HashCache.readCache(outputFolder, cachePath);
                    hashCache.caches.put(provider, providerCache);
                    hashCache.initialCount += providerCache.count();
                }
            });
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
        for(Map.Entry<String,DataProvider> entry : this.providersToRun.entrySet()){
            if(!this.alwaysGenerate && !hashCache.shouldRunInThisVersion(entry.getKey())){
                LOGGER.debug("Generator {} already run for version {}", entry.getKey(), (Object)this.version.getName());
                return;
            }
            if(entry.getValue() instanceof ResourceGenerator.DataProviderInstance){
                LOGGER.info("Running generator: {}", entry.getKey());
                stopwatch.start();
                ((ResourceGenerator.DataProviderInstance)entry.getValue()).generate();
                stopwatch.stop();
                LOGGER.info("{} finished after {} ms", entry.getKey(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
}
