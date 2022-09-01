package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.core.extensions.MinMaxBoundsExtension;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(MinMaxBounds.class)
public class MinMaxBoundsMixin implements MinMaxBoundsExtension {

    @Shadow
    private final Float min = null;
    @Shadow
    private final Float max = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public JsonElement coreLibSerialize(){
        if((Object)this == MinMaxBounds.UNBOUNDED)
            return JsonNull.INSTANCE;
        if(this.min == null || this.max == null || this.min.equals(this.max)){
            JsonObject valueJson = new JsonObject();
            if(this.min != null)
                valueJson.addProperty("min", this.min);
            if(this.max != null)
                valueJson.addProperty("max", this.max);
            return valueJson;
        }
        return new JsonPrimitive(this.min);
    }
}
