package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Deque;
import java.util.Map;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @SuppressWarnings("ConstantConditions")
    @ModifyVariable(
        method = "loadModels",
        at = @At(value = "STORE"),
        ordinal = 0
    )
    private Deque<ResourceLocation> loadModels(Deque<ResourceLocation> deque){
        Map<ResourceLocation,ModelBlock> models = ((ModelBakery)(Object)this).models;
        ClientRegistrationHandler.registerAllSpecialModels(model -> {
            if(!models.containsKey(model))
                deque.add(model);
        });
        return deque;
    }
}
