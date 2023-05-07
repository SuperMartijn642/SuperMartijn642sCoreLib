package com.supermartijn642.core.generator;

import com.supermartijn642.core.generator.aggregator.TranslationsAggregator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created 04/08/2022 by SuperMartijn642
 */
public abstract class LanguageGenerator extends ResourceGenerator {

    private final Map<String,String> translations = new LinkedHashMap<>();
    protected final String langCode;

    public LanguageGenerator(String modid, ResourceCache cache, String langCode){
        super(modid, cache);
        if(!langCode.matches("[a-z]{2}_[a-z]{2}"))
            throw new IllegalArgumentException("Invalid lang code '" + langCode + "'!");
        this.langCode = langCode;
    }

    @Override
    public void save(){
        // Save the object to the cache
        this.cache.saveResource(ResourceType.ASSET, TranslationsAggregator.INSTANCE, this.translations, this.modid, "lang", this.langCode, ".json");
    }

    /**
     * Adds the given translation.
     * @param translationKey key for the translation
     * @param translation    the translation
     */
    protected void translation(String translationKey, String translation){
        if(translationKey.trim().isEmpty())
            throw new IllegalArgumentException("Translation key '" + translation + "' for translation '" + translation + "' must not be empty!");

        this.translations.put(translationKey, translation);
    }

    /**
     * Adds the given translation for the item group.
     * @param group       group to add the translation for
     * @param translation translation of the group name
     */
    protected void itemGroup(ItemGroup group, String translation){
        ITextComponent component = group.getDisplayName();
        if(component instanceof TranslationTextComponent)
            this.translation(((TranslationTextComponent)component).getKey(), translation);
        this.translation(group.langId, translation);
    }

    /**
     * Adds the given translation for the item.
     * @param item        item to add the translation for
     * @param translation translation of the item name
     */
    protected void item(Item item, String translation){
        this.translation(item.getDescriptionId(), translation);
    }

    /**
     * Adds the given translation for the item.
     * @param block       block to add the translation for
     * @param translation translation of the block name
     */
    protected void block(Block block, String translation){
        this.translation(block.getDescriptionId(), translation);
    }

    public String getName(){
        return this.modName + " Language Generator";
    }
}
