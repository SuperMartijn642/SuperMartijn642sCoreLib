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
    private Map<?, Holder.Reference<?>> byValue;
    @Shadow
    private Map<?, Lifecycle> lifecycles;
    @Unique
    private final Map<ResourceKey<?>,Object> keyToObject = new HashMap<>();
    @Unique
    private boolean registeringOverrides = false;
    @Unique
    private Holder.Reference<?> overwrittenReference;

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
            // Remove the old object
            Object oldValue = this.keyToObject.get(key);
            if(oldValue != null){
                this.toId.removeInt(oldValue);
                this.byValue.remove(oldValue);
                this.lifecycles.remove(oldValue);
            }
            // Redirect the old reference to the new reference's values
            Holder.Reference<?> newReference = this.byLocation.get(key.location());
            if(newReference != null && newReference != this.overwrittenReference)
                ((CoreLibHolderReference)this.overwrittenReference).supermartijn642corelibOverride(key, object);
        }

        this.keyToObject.put(key, object);
    }

    @Override
    public void supermartijn642corelibSetRegisterOverrides(boolean flag){
        this.registeringOverrides = flag;
    }
}
