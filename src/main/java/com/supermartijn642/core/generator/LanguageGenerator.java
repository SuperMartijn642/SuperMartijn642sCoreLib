package com.supermartijn642.core.generator;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public abstract class LanguageGenerator extends ResourceGenerator {

    private final Map<String,String> translations = new HashMap<>();
    protected final String langCode;

    public LanguageGenerator(String modid, ResourceCache cache, String langCode){
        super(modid, cache);
        this.langCode = langCode;
    }

    @Override
    public void finish(){
        // Convert all translations to a json object
        JsonObject object = new JsonObject();
        this.translations.forEach(object::addProperty);
        // Save the object to the cache
        this.cache.saveJsonResource(ResourceType.ASSET, object, this.modid, "lang", this.langCode);
    }

    protected void translation(String translationKey, String translation){
        this.translations.put(translationKey, translation);
    }

    protected void itemGroup(CreativeTabs group, String translation){
        this.translation(group.getTranslatedTabLabel(), translation);
    }

    protected void item(Item item, String translation){
        this.translation(item.getUnlocalizedName(), translation);
    }

    protected void block(Block block, String translation){
        this.translation(block.getUnlocalizedName(), translation);
    }

    public String getName(){
        return this.modName + " Language Generator";
    }
}
