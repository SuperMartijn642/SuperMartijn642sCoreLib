package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.core.extensions.NBTPredicateExtension;
import net.minecraft.advancements.critereon.NBTPredicate;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
@Mixin(NBTPredicate.class)
public class NBTPredicateMixin implements NBTPredicateExtension {

    @Shadow
    private final NBTTagCompound tag = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public JsonElement coreLibSerialize(){
        return (Object)this == NBTPredicate.ANY || this.tag == null ? JsonNull.INSTANCE : new JsonPrimitive(this.tag.toString());
    }
}
