package com.supermartijn642.core.mixin;

import com.google.common.base.Stopwatch;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
@Mixin(DataGenerator.class)
public class DataGeneratorMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private ResourceCache resourceCache;
    @Shadow
    @Final
    private List<IDataProvider> providers;
    @Shadow
    @Final
    private Path outputFolder;

    @Inject(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/data/DirectoryCache;keep(Ljava/nio/file/Path;)V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void runHead(CallbackInfo ci, DirectoryCache hashCache){
        GatherDataEvent.DataGeneratorConfig dataGeneratorConfig = DatagenModLoaderAccessor.getDataGeneratorConfig();
        if(dataGeneratorConfig != null){ // Some mods run data generators themselves
            dataGeneratorConfig.getMods().stream().filter(GeneratorRegistrationHandler::hasHandlerForModid).forEach(modid -> {
                GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(modid);
                DataGenerator dataGenerator = (DataGenerator)(Object)this;
                // Get the output folder
                Path outputFolder = this.outputFolder;
                // Create a ResourceCache instance
                if(this.resourceCache == null)
                    this.resourceCache = ResourceCache.wrap(DatagenModLoaderAccessor.getExistingFileHelper(), hashCache, outputFolder);
                ((ResourceCache.HashCacheWrapper)this.resourceCache).allowWrites(false);
                handler.registerProviders(dataGenerator, DatagenModLoaderAccessor.getExistingFileHelper(), this.resourceCache);
            });
        }
    }

    @Inject(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/base/Stopwatch;createUnstarted()Lcom/google/common/base/Stopwatch;",
            shift = At.Shift.AFTER
        )
    )
    private void runBeforeGenerators(CallbackInfo ci){
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        for(IDataProvider provider : this.providers){
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
            target = "Lnet/minecraft/data/DirectoryCache;purgeStaleAndWrite()V",
            shift = At.Shift.BEFORE
        )
    )
    private void runTail(CallbackInfo ci){
        if(this.resourceCache != null)
            ((ResourceCache.HashCacheWrapper)this.resourceCache).finish();
    }
}
