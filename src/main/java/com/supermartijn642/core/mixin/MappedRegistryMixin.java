package com.supermartijn642.core.mixin;

import com.mojang.serialization.Lifecycle;
import com.supermartijn642.core.extensions.CoreLibHolderReference;
import com.supermartijn642.core.extensions.CoreLibMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;

/**
 * Created 22/03/2023 by SuperMartijn642
 */
@Mixin(MappedRegistry.class)
public class MappedRegistryMixin implements CoreLibMappedRegistry {

    @Shadow
    private Function<?,Holder.Reference<?>> customHolderProvider;
    @Shadow
    private Map<ResourceKey<?>,Holder.Reference<?>> byKey;
    @Shadow
    private Map<Object,Holder.Reference<?>> byValue;
    @Unique
    private boolean registeringOverrides = false;
    @Unique
    private Holder.Reference<?> overwrittenReference;

    @Inject(
        method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/core/Holder;",
        at = @At("HEAD")
    )
    private void registerMappingHead(int id, ResourceKey<?> key, Object object, Lifecycle lifecycle, boolean checkDuplicates, CallbackInfoReturnable<Holder<?>> ci){
        if(this.registeringOverrides && this.customHolderProvider != null)
            this.overwrittenReference = this.byKey.remove(key);
    }

    @Inject(
        method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/core/Holder;",
        at = @At("TAIL")
    )
    private void registerMappingTail(int id, ResourceKey<?> key, Object object, Lifecycle lifecycle, boolean checkDuplicates, CallbackInfoReturnable<Holder<?>> ci){
        if(this.registeringOverrides && this.customHolderProvider != null && this.overwrittenReference != null){
            Holder.Reference<?> newReference = this.byKey.get(key);
            if(newReference != null && newReference != this.overwrittenReference){
                // Add back the old entry to the 'byValue' map
                this.byValue.put(this.overwrittenReference.value(), this.overwrittenReference);
                // Redirect the old reference to the new reference's values
                ((CoreLibHolderReference)this.overwrittenReference).supermartijn642corelibOverride(key, object);
            }
        }
    }

    @Override
    public void supermartijn642corelibSetRegisterOverrides(boolean flag){
        this.registeringOverrides = flag;
    }
}
