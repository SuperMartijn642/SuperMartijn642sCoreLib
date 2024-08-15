package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.CoreLibHolderReference;
import com.supermartijn642.core.extensions.CoreLibMappedRegistry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
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
    private ObjectList<Holder.Reference<?>> byId;
    @Shadow
    private Reference2IntMap<Object> toId;
    @Shadow
    private Map<ResourceLocation,Holder.Reference<?>> byLocation;
    @Shadow
    private Map<Object,Holder.Reference<?>> byValue;
    @Unique
    private final Map<ResourceKey<?>,Object> keyToObject = new HashMap<>();
    @Unique
    private boolean registeringOverrides = false;
    @Unique
    private Holder.Reference<?> overwrittenReference;

    @Inject(
        method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;",
        at = @At("HEAD")
    )
    private void registerMappingHead(ResourceKey<?> key, Object object, RegistrationInfo registrationInfo, CallbackInfoReturnable<Holder.Reference<?>> ci){
        if(this.registeringOverrides)
            this.overwrittenReference = this.byLocation.remove(key.location());
    }

    @Inject(
        method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;",
        at = @At("TAIL")
    )
    private void registerMappingTail(ResourceKey<?> key, Object object, RegistrationInfo registrationInfo, CallbackInfoReturnable<Holder.Reference<?>> ci){
        if(this.registeringOverrides){
            // Redirect the old reference to the new reference's values
            Holder.Reference<?> newReference = this.byLocation.get(key.location());
            if(newReference != null && newReference != this.overwrittenReference)
                ((CoreLibHolderReference)this.overwrittenReference).supermartijn642corelibOverride(key, object);
            // Remove the old object
            Object oldValue = this.keyToObject.get(key);
            if(oldValue != null){
                // Re-use the id of the old object
                int oldId = this.toId.getInt(oldValue);
                this.toId.put(object, oldId);
                this.byId.removeLast();
                // Create a dummy reference
                //noinspection unchecked, DataFlowIssue, rawtypes, deprecation
                Holder.Reference dummy = Holder.Reference.createIntrusive(((MappedRegistry)(Object)this).holderOwner(), oldValue);
                //noinspection unchecked
                dummy.bindKey(this.overwrittenReference.key());
                this.byValue.put(oldValue, dummy);
            }
        }

        this.keyToObject.put(key, object);
    }

    @Override
    public void supermartijn642corelibSetRegisterOverrides(boolean flag){
        this.registeringOverrides = flag;
    }
}
