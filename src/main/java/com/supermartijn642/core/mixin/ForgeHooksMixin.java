package com.supermartijn642.core.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.data.tag.CustomTagEntryLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Created 01/08/2022 by SuperMartijn642
 */
@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

    @Redirect(
        method = "canHarvestBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getHarvestLevel(Lnet/minecraftforge/common/ToolType;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/BlockState;)I"
        ),
        remap = false
    )
    private static int canHarvestBlockRedirect(ItemStack stack, ToolType type, PlayerEntity player, BlockState state){
        if(state.getBlock() instanceof BaseBlock){
            int bestHarvestLevel = -1;
            for(ToolType toolType : stack.getToolTypes()){
                if(state.isToolEffective(toolType)){
                    int harvestLevel = stack.getHarvestLevel(toolType, player, state);
                    if(harvestLevel > bestHarvestLevel)
                        bestHarvestLevel = harvestLevel;
                }
            }
            if(bestHarvestLevel == -1)
                bestHarvestLevel = stack.getHarvestLevel(type, player, state);
            return bestHarvestLevel;
        }
        return stack.getHarvestLevel(type, player, state);
    }

    @Inject(
        method = "deserializeTagAdditions",
        at = @At("HEAD"),
        remap = false
    )
    private static void deserializeTagAdditions(List<ITag.ITagEntry> list, JsonObject json, List<ITag.Proxy> allList, CallbackInfo ci){
        if(json.has("optional") && json.get("optional").isJsonArray()){
            JsonArray optionalArray = json.getAsJsonArray("optional");
            for(int i = 0; i < optionalArray.size(); i++){
                ITag.ITagEntry entry = CustomTagEntryLoader.potentiallyDeserialize(optionalArray.get(i));
                if(entry != null){
                    optionalArray.remove(i);
                    list.add(entry);
                    i--;
                }
            }
        }
        if(json.has("remove") && json.get("remove").isJsonArray()){
            JsonArray removeArray = json.getAsJsonArray("remove");
            for(int i = 0; i < removeArray.size(); i++){
                ITag.ITagEntry entry = CustomTagEntryLoader.potentiallyDeserialize(removeArray.get(i));
                if(entry != null){
                    removeArray.remove(i);
                    i--;
                }
            }
        }
    }
}
