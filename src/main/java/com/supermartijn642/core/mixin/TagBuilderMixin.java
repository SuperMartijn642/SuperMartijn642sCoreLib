package com.supermartijn642.core.mixin;

import com.google.gson.JsonElement;
import com.supermartijn642.core.data.tag.CustomTagEntries;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 11/02/2024 by SuperMartijn642
 */
@Mixin(Tag.Builder.class)
public class TagBuilderMixin {

    @Inject(
        method = "parseEntry(Lcom/google/gson/JsonElement;)Lnet/minecraft/tags/ITag$ITagEntry;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void parseEntry(JsonElement element, CallbackInfoReturnable<ITag.ITagEntry> ci){
        ITag.ITagEntry entry = CustomTagEntries.potentiallyDeserialize(element);
        if(entry != null)
            ci.setReturnValue(entry);
    }
}
