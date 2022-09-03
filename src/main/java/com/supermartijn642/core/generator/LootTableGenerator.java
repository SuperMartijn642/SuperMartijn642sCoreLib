package com.supermartijn642.core.generator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created 20/08/2022 by SuperMartijn642
 */
public abstract class LootTableGenerator extends ResourceGenerator {

    private static final Gson GSON = LootTableManager.GSON;

    private final Map<ResourceLocation,LootTableBuilder> lootTables = new HashMap<>();

    public LootTableGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        // Loop over all loot tables
        for(LootTableBuilder lootTableBuilder : this.lootTables.values()){
            JsonObject json = new JsonObject();
            // Type
            if(lootTableBuilder.parameters != LootParameterSets.ALL_PARAMS)
                json.addProperty("type", LootParameterSets.getKey(lootTableBuilder.parameters).toString());
            // Functions
            if(!lootTableBuilder.functions.isEmpty()){
                JsonArray functionsJson = new JsonArray();
                for(ILootFunction function : lootTableBuilder.functions)
                    functionsJson.add(GSON.toJsonTree(function));
                json.add("functions", functionsJson);
            }
            // Pools
            if(!lootTableBuilder.pools.isEmpty()){
                JsonArray poolsJson = new JsonArray();
                // Loop over all pools
                for(LootPoolBuilder pool : lootTableBuilder.pools){
                    JsonObject poolJson = new JsonObject();
                    // Name
                    if(pool.name != null && !pool.name.isEmpty())
                        poolJson.addProperty("name", pool.name);
                    // Rolls
                    poolJson.add("rolls", GSON.toJsonTree(pool.rolls));
                    // Bonus rolls
                    if(!(pool.bonusRolls instanceof ConstantRange) || pool.bonusRolls.getInt(null) != 0)
                        poolJson.add("bonus_rolls", GSON.toJsonTree(pool.bonusRolls));
                    // Conditions
                    if(!pool.conditions.isEmpty()){
                        JsonArray conditionsJson = new JsonArray();
                        for(ILootCondition condition : pool.conditions)
                            conditionsJson.add(GSON.toJsonTree(condition));
                        poolJson.add("conditions", conditionsJson);
                    }
                    // Functions
                    if(!pool.functions.isEmpty()){
                        JsonArray functionsJson = new JsonArray();
                        for(ILootFunction function : pool.functions)
                            functionsJson.add(GSON.toJsonTree(function));
                        poolJson.add("functions", functionsJson);
                    }
                    // Entries
                    if(pool.entries.isEmpty())
                        throw new RuntimeException("Loot table '" + lootTableBuilder.identifier + "' has loot pool without any entries!");
                    JsonArray entriesJson = new JsonArray();
                    for(LootEntry entry : pool.entries)
                        entriesJson.add(GSON.toJsonTree(entry));
                    poolJson.add("entries", entriesJson);

                    poolsJson.add(poolJson);
                }
                json.add("pools", poolsJson);
            }

            // Save the object to the cache
            ResourceLocation identifier = lootTableBuilder.identifier;
            this.cache.saveJsonResource(ResourceType.DATA, json, identifier.getNamespace(), "loot_tables", identifier.getPath());
        }
    }

    /**
     * Gets a loot table builder for the given identifier. The returned loot table builder may be a new loot table builder or an existing one if requested before.
     * @param identifier resource location of the loot table
     */
    protected LootTableBuilder lootTable(ResourceLocation identifier){
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, identifier.getNamespace(), "loot_tables", identifier.getPath(), ".json");
        return this.lootTables.computeIfAbsent(identifier, LootTableBuilder::new);
    }

    /**
     * Gets a loot table builder for the given namespace and path. The returned loot table builder may be a new loot table builder or an existing one if requested before.
     * @param namespace namespace of the loot table
     * @param path      path of the loot table
     */
    protected LootTableBuilder lootTable(String namespace, String path){
        return this.lootTable(new ResourceLocation(namespace, path));
    }

    /**
     * Gets a loot table builder for the given block. The returned loot table builder may be a new loot table builder or an existing one if requested before.
     * @param block block to create the loot table for
     */
    protected LootTableBuilder lootTable(Block block){
        return this.lootTable(block.getLootTable());
    }

    /**
     * Creates a basic loot table for the given block to drop itself when broken.
     * @param block block to create the loot table for
     */
    protected LootTableBuilder dropSelf(Block block){
        return this.lootTable(block.getLootTable()).parameters(LootParameterSets.BLOCK).pool(poolBuilder -> poolBuilder.survivesExplosionCondition().itemEntry(block));
    }

    @Override
    public String getName(){
        return this.modName + " Loot Table Generator";
    }

    protected static class LootTableBuilder {

        protected final ResourceLocation identifier;
        private final List<LootPoolBuilder> pools = new ArrayList<>();
        private final List<ILootFunction> functions = new ArrayList<>();
        private LootParameterSet parameters = LootParameterSets.ALL_PARAMS;

        public LootTableBuilder(ResourceLocation identifier){
            this.identifier = identifier;
        }

        /**
         * Sets the loot table type to the given parameter set.
         */
        public LootTableBuilder parameters(LootParameterSet parameters){
            if(LootParameterSets.getKey(parameters) == null)
                throw new IllegalArgumentException("Cannot use unregistered parameter set '" + parameters + "'!");

            this.parameters = parameters;
            return this;
        }

        /**
         * Sets the loot table type to the block parameter set.
         */
        public LootTableBuilder blockParameters(){
            return this.parameters(LootParameterSets.BLOCK);
        }

        /**
         * Sets the loot table type to the chest parameter set.
         */
        public LootTableBuilder chestParameters(){
            return this.parameters(LootParameterSets.CHEST);
        }

        /**
         * Constructs a new loot pool for this loot table.
         * @param poolBuilderConsumer consumer to build the loot pool
         */
        public LootTableBuilder pool(Consumer<LootPoolBuilder> poolBuilderConsumer){
            LootPoolBuilder poolBuilder = new LootPoolBuilder();
            poolBuilderConsumer.accept(poolBuilder);
            this.pools.add(poolBuilder);
            return this;
        }

        /**
         * Adds the given item function to this loot table.
         * @param function item function to be added
         */
        public LootTableBuilder function(ILootFunction function){
            if(!LootFunctionManager.FUNCTIONS_BY_CLASS.containsKey(function.getClass()))
                throw new IllegalArgumentException("Cannot use unregistered item function '" + function + "'!");

            this.functions.add(function);
            return this;
        }
    }

    protected static class LootPoolBuilder {

        private final List<ILootCondition> conditions = new ArrayList<>();
        private final List<ILootFunction> functions = new ArrayList<>();
        private final List<LootEntry> entries = new ArrayList<>();
        private IRandomRange rolls = ConstantRange.exactly(1);
        private IRandomRange bonusRolls = ConstantRange.exactly(0);
        private String name;

        /**
         * Sets the number provider for the number of rolls for this loot pool.
         * @param provider number provider for number of rolls
         */
        public LootPoolBuilder rolls(IRandomRange provider){
            this.rolls = provider;
            return this;
        }

        /**
         * Sets the number provider for the number of rolls to a constant with the given value.
         * @param rolls number of rolls
         */
        public LootPoolBuilder constantRolls(int rolls){
            return this.rolls(ConstantRange.exactly(rolls));
        }

        /**
         * Sets the number provider for the number of rolls to a uniform chance between the given minimum and maximum.
         * @param min minimum number of rolls
         * @param max maximum number of rolls
         */
        public LootPoolBuilder uniformRolls(int min, int max){
            return this.rolls(RandomValueRange.between(min, max));
        }

        /**
         * Sets the number provider for the number of rolls to a binomial distribution with the given chance and attempts.
         * @param n number of attempts
         * @param p chance that an attempt succeeds
         */
        public LootPoolBuilder binomialRolls(int n, int p){
            return this.rolls(BinomialRange.binomial(n, p));
        }

        /**
         * Sets the number provider for the number of bonus rolls for this loot pool.
         * @param provider number provider for number of bonus rolls
         */
        public LootPoolBuilder bonusRolls(IRandomRange provider){
            this.bonusRolls = provider;
            return this;
        }

        /**
         * Sets the number provider for the number of bonus rolls to a constant with the given value.
         * @param rolls number of bonus rolls
         */
        public LootPoolBuilder constantBonusRolls(int rolls){
            return this.bonusRolls(ConstantRange.exactly(rolls));
        }

        /**
         * Sets the number provider for the number of bonus rolls to a uniform chance between the given minimum and maximum.
         * @param min minimum number of bonus rolls
         * @param max maximum number of bonus rolls
         */
        public LootPoolBuilder uniformBonusRolls(int min, int max){
            return this.bonusRolls(RandomValueRange.between(min, max));
        }

        /**
         * Sets the number provider for the number of bonus rolls to a binomial distribution with the given chance and attempts.
         * @param n number of attempts
         * @param p chance that an attempt succeeds
         */
        public LootPoolBuilder binomialBonusRolls(int n, int p){
            return this.bonusRolls(BinomialRange.binomial(n, p));
        }

        /**
         * Sets the name for this loot pool.
         * @param name name for the pool
         */
        public LootPoolBuilder name(String name){
            this.name = name;
            return this;
        }

        /**
         * Adds the given item condition to this loot pool.
         * @param condition condition to be added
         */
        public LootPoolBuilder condition(ILootCondition condition){
            if(!LootConditionManager.CONDITIONS_BY_CLASS.containsKey(condition.getClass()))
                throw new IllegalArgumentException("Cannot use unregistered loot pool condition '" + condition + "'!");

            this.conditions.add(condition);
            return this;
        }

        /**
         * Adds a survives explosion condition to this loot pool
         */
        public LootPoolBuilder survivesExplosionCondition(){
            return this.condition(SurvivesExplosion.survivesExplosion().build());
        }

        /**
         * Adds an entry to this loot pool.
         * @param entry entry to be added
         */
        public LootPoolBuilder entry(LootEntry entry){
            if(!LootEntryManager.CLASS_TO_SERIALIZER.containsKey(entry.getClass()))
                throw new IllegalArgumentException("Cannot use unregistered loot pool entry '" + entry + "'!");

            this.entries.add(entry);
            return this;
        }

        /**
         * Adds an empty entry to this loot pool.
         */
        public LootPoolBuilder emptyEntry(){
            return this.entry(EmptyLootEntry.emptyItem().build());
        }

        /**
         * Adds an item entry to this loot pool.
         * @param item item to be added as an entry
         */
        public LootPoolBuilder itemEntry(IItemProvider item){
            return this.entry(ItemLootEntry.lootTableItem(item).build());
        }

        /**
         * Adds an item entry to this loot pool.
         * @param item item to be added as an entry
         */
        public LootPoolBuilder itemEntry(ResourceLocation item){
            if(!Registries.ITEMS.hasIdentifier(item))
                throw new IllegalArgumentException("Could not find any item registered under '" + item + "'!");

            return this.itemEntry(Registries.ITEMS.getValue(item));
        }

        /**
         * Adds an item entry to this loot pool.
         * @param namespace  namespace of the item to be added as an entry
         * @param identifier path of the item to be added as an entry
         */
        public LootPoolBuilder itemEntry(String namespace, String identifier){
            return this.itemEntry(new ResourceLocation(namespace, identifier));
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param tag tag to be added as an entry
         */
        public LootPoolBuilder tagEntry(Tag<Item> tag){
            return this.entry(TagLootEntry.expandTag(tag).build());
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param tag tag to be added as an entry
         */
        public LootPoolBuilder tagEntry(ResourceLocation tag){
            return this.tagEntry(new ItemTags.Wrapper(tag));
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param namespace namespace of the tag to be added as an entry
         * @param path      path of the tag to be added as an entry
         */
        public LootPoolBuilder tagEntry(String namespace, String path){
            return this.tagEntry(new ResourceLocation(namespace, path));
        }

        /**
         * Adds a loot table entry to this loot pool.
         * @param lootTable loot table to be added as an entry
         */
        public LootPoolBuilder lootTableEntry(ResourceLocation lootTable){
            return this.entry(TableLootEntry.lootTableReference(lootTable).build());
        }

        /**
         * Adds a loot table entry to this loot pool.
         * @param namespace namespace of the loot table to be added as an entry
         * @param path      path of the loot table to be added as an entry
         */
        public LootPoolBuilder lootTableEntry(String namespace, String path){
            return this.lootTableEntry(new ResourceLocation(namespace, path));
        }

        /**
         * Adds an item function to this loot table.
         * @param function item function to be added
         */
        public LootPoolBuilder function(ILootFunction function){
            if(!LootFunctionManager.FUNCTIONS_BY_CLASS.containsKey(function.getClass()))
                throw new IllegalArgumentException("Cannot use unregistered item function '" + function + "'!");

            this.functions.add(function);
            return this;
        }
    }
}
