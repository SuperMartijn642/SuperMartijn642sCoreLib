package com.supermartijn642.core.mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.Variant;
import org.joml.Math;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Variant.Deserializer.class)
public class VariantSimpleModelStateMixin {

    @ModifyExpressionValue(
        method = "deserialize",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/BlockModelRotation;getRotation()Lcom/mojang/math/Transformation;"
        )
    )
    private Transformation readZRotation(Transformation xyRotation, @Local JsonObject json){
        if(!json.has("supermartijn642corelib:z")
            || !json.get("supermartijn642corelib:z").isJsonPrimitive()
            || !json.getAsJsonPrimitive("supermartijn642corelib:z").isNumber())
            return xyRotation;
        int z = ((json.get("supermartijn642corelib:z").getAsInt() / 90) % 4 + 4) % 4;
        Quaternionf rotation = new Quaternionf()
            .rotateZ(-z * Math.PI_OVER_2_f)
            .mul(xyRotation.getLeftRotation());
        return new Transformation(null, rotation, null, null);
    }
}
