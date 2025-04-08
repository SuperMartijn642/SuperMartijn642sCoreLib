package com.supermartijn642.core.mixin.dev;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.core.extensions.DataGeneratorConfigExtension;
import com.supermartijn642.core.extensions.DataGeneratorExtension;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Created 16/10/2023 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(value = GatherDataEvent.DataGeneratorConfig.class, remap = false)
public class DataGeneratorConfigMixin implements DataGeneratorConfigExtension {

    @Unique
    private List<Path> existingPaths;
    @Final
    @Shadow
    private ResourceManager clientResourceManager;
    @Final
    @Shadow
    private ResourceManager serverResourceManager;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(CallbackInfo ci, @Local(ordinal = 1) Collection<Path> existingPaths){
        this.existingPaths = List.copyOf(existingPaths);
    }

    @Inject(
        method = "makeGenerator",
        at = @At("RETURN")
    )
    public void makeGenerator(CallbackInfoReturnable<DataGenerator> ci){
        DataGenerator generator = ci.getReturnValue();
        if(generator != null)
            //noinspection DataFlowIssue
            ((DataGeneratorExtension)generator).setDataGeneratorConfig((GatherDataEvent.DataGeneratorConfig)(Object)this);
    }

    @Override
    public List<Path> supermartijn642corelibGetExistingPaths(){
        return this.existingPaths;
    }

    @Override
    public ResourceManager supermartijn642corelibGetClientResources(){
        return this.clientResourceManager;
    }

    @Override
    public ResourceManager supermartijn642corelibGetServerResources(){
        return this.serverResourceManager;
    }
}
