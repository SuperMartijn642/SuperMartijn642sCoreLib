package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.CoreLibHolderReference;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 22/03/2023 by SuperMartijn642
 */
@Mixin(Holder.Reference.class)
public class HolderReferenceMixin implements CoreLibHolderReference {

    @Shadow
    private ResourceKey<?> key;
    @Shadow
    private Object value;

    @Override
    public void supermartijn642corelibOverride(ResourceKey<?> key, Object value){
        this.key = key;
        this.value = value;
    }
}
