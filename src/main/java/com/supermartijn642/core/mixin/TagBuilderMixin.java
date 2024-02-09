package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.tag.CustomTagEntryLoader;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Created 11/02/2024 by SuperMartijn642
 */
@Mixin(Tag.Builder.class)
public class TagBuilderMixin {

    @Unique
    private List<Tag.ITagEntry<?>> customEntries;
    @Final
    @Shadow
    private Set<Tag.ITagEntry<?>> values;

    @Inject(
        method = "addFromJson(Ljava/util/function/Function;Lcom/google/gson/JsonObject;)Lnet/minecraft/tags/Tag$Builder;",
        at = @At("HEAD")
    )
    private void addFromJsonHead(Function<ResourceLocation,Optional<?>> elementLookup, JsonObject json, CallbackInfoReturnable<Tag.Builder<?>> ci){
        if(!json.has("values") || !json.get("values").isJsonArray())
            return;
        JsonArray values = json.getAsJsonArray("values");
        this.customEntries = null;
        for(int i = 0; i < values.size(); i++){
            Tag.ITagEntry<?> entry = CustomTagEntryLoader.potentiallyDeserialize(values.get(i));
            if(entry != null){
                if(this.customEntries == null)
                    this.customEntries = new ArrayList<>();
                this.customEntries.add(entry);
            }
        }
    }

    @Inject(
        method = "addFromJson(Ljava/util/function/Function;Lcom/google/gson/JsonObject;)Lnet/minecraft/tags/Tag$Builder;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/ForgeHooks;deserializeTagAdditions(Lnet/minecraft/tags/Tag$Builder;Ljava/util/function/Function;Lcom/google/gson/JsonObject;)V",
            shift = At.Shift.BEFORE,
            remap = false
        )
    )
    private void addFromJsonTail(Function<ResourceLocation,Optional<?>> elementLookup, JsonObject json, CallbackInfoReturnable<Tag.Builder<?>> ci){
        if(this.customEntries != null){
            this.values.addAll(this.customEntries);
            this.customEntries = null;
        }
    }
}
