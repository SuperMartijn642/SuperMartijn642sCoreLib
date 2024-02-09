package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.tag.CustomTagEntryLoader;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Created 11/02/2024 by SuperMartijn642
 */
@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

    @Inject(
        method = "deserializeTagAdditions",
        at = @At("HEAD"),
        remap = false
    )
    private static void deserializeTagAdditions(List<Tag.Entry> list, JsonObject json, List<Tag.BuilderEntry> allList, CallbackInfo ci){
        if(json.has("remove") && json.get("remove").isJsonArray()){
            JsonArray removeArray = json.getAsJsonArray("remove");
            for(int i = 0; i < removeArray.size(); i++){
                Tag.Entry entry = CustomTagEntryLoader.potentiallyDeserialize(removeArray.get(i));
                if(entry != null){
                    removeArray.remove(i);
                    i--;
                }
            }
        }
    }
}
