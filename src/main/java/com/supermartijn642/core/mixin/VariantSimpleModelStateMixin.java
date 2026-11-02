package com.supermartijn642.core.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.Variant;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
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
        Quat4f zRotation = new Quat4f();
        zRotation.set(new AxisAngle4f(0, 0, 1, -z * (float)(Math.PI / 2)));
        ModelStateComposition rotation = new ModelStateComposition(new TRSRTransformation(null, zRotation, null, null), variant.getRotation());
        ci.setReturnValue(new Variant(variant.getModelLocation(), variant.getRotation(), variant.isUvLocked(), variant.getWeight()) {
            @Override
            public IModelState getState(){
                return rotation;
            }
        });
    }
}
