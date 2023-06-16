package com.supermartijn642.core.mixin;

import com.mojang.serialization.Lifecycle;
import com.supermartijn642.core.extensions.CoreLibHolderReference;
import com.supermartijn642.core.extensions.CoreLibMappedRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created 22/03/2023 by SuperMartijn642
 */
@Mixin(MappedRegistry.class)
public class MappedRegistryMixin implements CoreLibMappedRegistry {

    @Shadow
    private Object2IntMap<?> toId;
    @Shadow
    private Map<ResourceLocation,Holder.Reference<?>> byLocation;
    @Shadow
    private Map<Object,Holder.Reference<?>> byValue;
    @Shadow
    private Map<?,Lifecycle> lifecycles;
    @Unique
    private final Map<ResourceKey<?>,Object> keyToObject = new HashMap<>();
    @Unique
    private boolean registeringOverrides = false;
    @Unique
    private Holder.Reference<?> overwrittenReference;
    private BiConsumer<Object,Object> overrideConsumer;

    @Inject(
        method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;",
        at = @At("HEAD")
    )
    private void registerMappingHead(int id, ResourceKey<?> key, Object object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<?>> ci){
        if(this.registeringOverrides)
            this.overwrittenReference = this.byLocation.remove(key.location());
    }

    @Inject(
        method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;",
        at = @At("TAIL")
    )
    private void registerMappingTail(int id, ResourceKey<?> key, Object object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<?>> ci){
        if(this.registeringOverrides){
            // Redirect the old reference to the new reference's values
            Holder.Reference<?> newReference = this.byLocation.get(key.location());
            if(newReference != null && newReference != this.overwrittenReference)
                ((CoreLibHolderReference)this.overwrittenReference).supermartijn642corelibOverride(key, object);
            // Remove the old object
            Object oldValue = this.keyToObject.get(key);
            if(oldValue != null){
                this.toId.removeInt(oldValue);
                // Create a dummy reference
                //noinspection unchecked, DataFlowIssue, rawtypes, deprecation
                Holder.Reference dummy = Holder.Reference.createIntrusive(((MappedRegistry)(Object)this).holderOwner(), oldValue);
                //noinspection unchecked
                dummy.bindKey(this.overwrittenReference.key());
                this.byValue.put(oldValue, dummy);
                this.lifecycles.remove(oldValue);

                // Call the override consumer
                if(this.overrideConsumer != null)
                    this.overrideConsumer.accept(oldValue, object);
            }
        }

        this.keyToObject.put(key, object);
    }

    @Override
    public void supermartijn642corelibSetRegisterOverrides(boolean flag, BiConsumer<Object,Object> overrideConsumer){
        this.registeringOverrides = flag;
        this.overrideConsumer = overrideConsumer;
    }
}
