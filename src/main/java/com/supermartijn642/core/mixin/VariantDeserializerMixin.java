package com.supermartijn642.core.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import java.lang.reflect.Type;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
@Mixin(Variant.Deserializer.class)
public class VariantDeserializerMixin {

    @Shadow
    protected ModelRotation parseModelRotation(JsonObject json){
        return null;
    }

    @Shadow
    protected int parseWeight(JsonObject json){
        return 0;
    }

    @Inject(
        method = "deserialize",
        at = @At("HEAD"),
        cancellable = true
    )
    private void deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context, CallbackInfoReturnable<Variant> ci){
        JsonObject json = jsonElement.getAsJsonObject();
        if(json.has("type") && json.get("type").isJsonPrimitive() && json.getAsJsonPrimitive("type").isString() && json.get("type").getAsString().equals("supermartijn642corelib:any_model_location")){
            // This is purely to remove the 'block/' folder prefix for models
            ResourceLocation modelLocation = new ResourceLocation(JsonUtils.getString(json, "model"));
            ModelRotation modelRotation = this.parseModelRotation(json);
            boolean uvLock = JsonUtils.getBoolean(json, "uvlock", false);
            int weight = this.parseWeight(json);
            // Add z rotation
            if(json.has("supermartijn642corelib:z")
                && json.get("supermartijn642corelib:z").isJsonPrimitive()
                && json.getAsJsonPrimitive("supermartijn642corelib:z").isNumber()){
                int z = ((json.get("supermartijn642corelib:z").getAsInt() / 90) % 4 + 4) % 4;
                Quat4f zRotation = new Quat4f();
                zRotation.set(new AxisAngle4f(0, 0, 1, -z * (float)(Math.PI / 2)));
                ModelStateComposition rotation = new ModelStateComposition(new TRSRTransformation(null, zRotation, null, null), modelRotation);
                ci.setReturnValue(new Variant(modelLocation, modelRotation, uvLock, weight) {
                    @Override
                    public IModelState getState(){
                        return rotation;
                    }
                });
            }
            ci.setReturnValue(new Variant(modelLocation, modelRotation, uvLock, weight));
        }
    }
}
