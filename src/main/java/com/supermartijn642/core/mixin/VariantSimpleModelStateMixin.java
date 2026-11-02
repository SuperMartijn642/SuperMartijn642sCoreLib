package com.supermartijn642.core.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Variant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(Variant.Deserializer.class)
public class VariantSimpleModelStateMixin {

    @Inject(
        method = "deserialize",
        at = @At("RETURN"),
        cancellable = true
    )
    private void readZRotation(JsonElement element, Type type, JsonDeserializationContext context, CallbackInfoReturnable<Variant> ci){
        if(!element.isJsonObject())
            return;
        JsonObject json = element.getAsJsonObject();
        if(!json.has("supermartijn642corelib:z")
            || !json.get("supermartijn642corelib:z").isJsonPrimitive()
            || !json.getAsJsonPrimitive("supermartijn642corelib:z").isNumber())
            return;
        Variant variant = ci.getReturnValue();
        if(variant == null)
            return;
        int z = ((json.get("supermartijn642corelib:z").getAsInt() / 90) % 4 + 4) % 4;
        Quaternion rotation = Vector3f.ZP.rotationDegrees(-z);
        rotation.mul(variant.getRotation().getLeftRotation());
        ci.setReturnValue(new Variant(
            variant.getModelLocation(),
            new TransformationMatrix(null, rotation, null, null),
            variant.isUvLocked(),
            variant.getWeight()
        ));
    }
}
