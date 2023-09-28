package com.supermartijn642.core.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created 20/08/2022 by SuperMartijn642
 */
public abstract class LootTableGenerator extends ResourceGenerator {

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
            if(lootTableBuilder.parameters != LootContextParamSets.ALL_PARAMS)
                json.add("type", LootContextParamSets.CODEC.encodeStart(JsonOps.INSTANCE, lootTableBuilder.parameters).getOrThrow(false, s -> {}));
            // Functions
            if(!lootTableBuilder.functions.isEmpty()){
                JsonArray functionsJson = new JsonArray();
                for(LootItemFunction function : lootTableBuilder.functions)
                    functionsJson.add(LootItemFunctions.CODEC.encodeStart(JsonOps.INSTANCE, function).getOrThrow(false, s -> {}));
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
                    poolJson.add("rolls", NumberProviders.CODEC.encodeStart(JsonOps.INSTANCE, pool.rolls).getOrThrow(false, s -> {}));
                    // Bonus rolls
                    if(!(pool.bonusRolls instanceof ConstantValue) || pool.bonusRolls.getInt(null) != 0)
                        poolJson.add("bonus_rolls", NumberProviders.CODEC.encodeStart(JsonOps.INSTANCE, pool.bonusRolls).getOrThrow(false, s -> {}));
                    // Conditions
                    if(!pool.conditions.isEmpty()){
                        JsonArray conditionsJson = new JsonArray();
                        for(LootItemCondition condition : pool.conditions)
                            conditionsJson.add(LootItemConditions.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(false, s -> {}));
                        poolJson.add("conditions", conditionsJson);
                    }
                    // Functions
                    if(!pool.functions.isEmpty()){
                        JsonArray functionsJson = new JsonArray();
                        for(LootItemFunction function : pool.functions)
                            functionsJson.add(LootItemFunctions.CODEC.encodeStart(JsonOps.INSTANCE, function).getOrThrow(false, s -> {}));
                        poolJson.add("functions", functionsJson);
                    }
                    // Entries
                    if(pool.entries.isEmpty())
                        throw new RuntimeException("Loot table '" + lootTableBuilder.identifier + "' has loot pool without any entries!");
                    JsonArray entriesJson = new JsonArray();
                    for(LootPoolEntryContainer entry : pool.entries)
                        entriesJson.add(LootPoolEntries.CODEC.encodeStart(JsonOps.INSTANCE, entry).getOrThrow(false, s -> {}));
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
        return this.lootTable(block).blockParameters().pool(poolBuilder -> poolBuilder.survivesExplosionCondition().itemEntry(block));
    }

    /**
     * Creates a basic loot table for the given to drop itself when broken with a silk touch tool.
     * @param block block to create the loot table for
     */
    protected LootTableBuilder dropSelfWhenSilkTouch(Block block){
        return this.lootTable(block).blockParameters().pool(poolBuilder -> poolBuilder.hasEnchantmentCondition(Enchantments.SILK_TOUCH).itemEntry(block));
    }

    @Override
    public String getName(){
        return this.modName + " Loot Table Generator";
    }

    public static class LootTableBuilder {

        protected final ResourceLocation identifier;
        private final List<LootPoolBuilder> pools = new ArrayList<>();
        private final List<LootItemFunction> functions = new ArrayList<>();
        private LootContextParamSet parameters = LootContextParamSets.ALL_PARAMS;

        protected LootTableBuilder(ResourceLocation identifier){
            this.identifier = identifier;
        }

        /**
         * Sets the loot table type to the given parameter set.
         */
        public LootTableBuilder parameters(LootContextParamSet parameters){
            if(LootContextParamSets.CODEC.encodeStart(JsonOps.INSTANCE, parameters).error().isPresent())
                throw new IllegalArgumentException("Cannot use unregistered parameter set '" + parameters + "'!");

            this.parameters = parameters;
            return this;
        }

        /**
         * Sets the loot table type to the block parameter set.
         */
        public LootTableBuilder blockParameters(){
            return this.parameters(LootContextParamSets.BLOCK);
        }

        /**
         * Sets the loot table type to the chest parameter set.
         */
        public LootTableBuilder chestParameters(){
            return this.parameters(LootContextParamSets.CHEST);
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
        public LootTableBuilder function(LootItemFunction function){
            if(BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType()) == null)
                throw new IllegalArgumentException("Cannot use unregistered item function '" + function + "'!");

            this.functions.add(function);
            return this;
        }
    }

    public static class LootPoolBuilder {

        private final List<LootItemCondition> conditions = new ArrayList<>();
        private final List<LootItemFunction> functions = new ArrayList<>();
        private final List<LootPoolEntryContainer> entries = new ArrayList<>();
        private NumberProvider rolls = ConstantValue.exactly(1);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);
        private String name;

        protected LootPoolBuilder(){
        }

        /**
         * Sets the number provider for the number of rolls for this loot pool.
         * @param provider number provider for number of rolls
         */
        public LootPoolBuilder rolls(NumberProvider provider){
            if(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.getKey(provider.getType()) == null)
                throw new IllegalArgumentException("Cannot use unregistered number provider '" + provider + "'!");

            this.rolls = provider;
            return this;
        }

        /**
         * Sets the number provider for the number of rolls to a constant with the given value.
         * @param rolls number of rolls
         */
        public LootPoolBuilder constantRolls(int rolls){
            return this.rolls(ConstantValue.exactly(rolls));
        }

        /**
         * Sets the number provider for the number of rolls to a uniform chance between the given minimum and maximum.
         * @param min minimum number of rolls
         * @param max maximum number of rolls
         */
        public LootPoolBuilder uniformRolls(int min, int max){
            return this.rolls(UniformGenerator.between(min, max));
        }

        /**
         * Sets the number provider for the number of rolls to a binomial distribution with the given chance and attempts.
         * @param n number of attempts
         * @param p chance that an attempt succeeds
         */
        public LootPoolBuilder binomialRolls(int n, int p){
            return this.rolls(BinomialDistributionGenerator.binomial(n, p));
        }

        /**
         * Sets the number provider for the number of bonus rolls for this loot pool.
         * @param provider number provider for number of bonus rolls
         */
        public LootPoolBuilder bonusRolls(NumberProvider provider){
            if(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.getKey(provider.getType()) == null)
                throw new IllegalArgumentException("Cannot use unregistered number provider '" + provider + "'!");

            this.bonusRolls = provider;
            return this;
        }

        /**
         * Sets the number provider for the number of bonus rolls to a constant with the given value.
         * @param rolls number of bonus rolls
         */
        public LootPoolBuilder constantBonusRolls(int rolls){
            return this.bonusRolls(ConstantValue.exactly(rolls));
        }

        /**
         * Sets the number provider for the number of bonus rolls to a uniform chance between the given minimum and maximum.
         * @param min minimum number of bonus rolls
         * @param max maximum number of bonus rolls
         */
        public LootPoolBuilder uniformBonusRolls(int min, int max){
            return this.bonusRolls(UniformGenerator.between(min, max));
        }

        /**
         * Sets the number provider for the number of bonus rolls to a binomial distribution with the given chance and attempts.
         * @param n number of attempts
         * @param p chance that an attempt succeeds
         */
        public LootPoolBuilder binomialBonusRolls(int n, int p){
            return this.bonusRolls(BinomialDistributionGenerator.binomial(n, p));
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
        public LootPoolBuilder condition(LootItemCondition condition){
            if(BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType()) == null)
                throw new IllegalArgumentException("Cannot use unregistered loot pool condition '" + condition + "'!");

            this.conditions.add(condition);
            return this;
        }

        /**
         * Adds a survives explosion condition to this loot pool
         */
        public LootPoolBuilder survivesExplosionCondition(){
            return this.condition(ExplosionCondition.survivesExplosion().build());
        }

        /**
         * Adds a condition for the used tool to have the given enchantment.
         * @param enchantment enchantment required
         * @param minLevel    minimum level of the enchantment (inclusive)
         * @param maxLevel    maximum level of the enchantment (inclusive)
         */
        public LootPoolBuilder hasEnchantmentCondition(Enchantment enchantment, int minLevel, int maxLevel){
            return this.condition(MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(enchantment, MinMaxBounds.Ints.between(minLevel, maxLevel)))).build());
        }

        /**
         * Adds a condition for the used tool to have the given enchantment.
         * @param enchantment enchantment required
         * @param minLevel    minimum level of the enchantment
         */
        public LootPoolBuilder hasEnchantmentCondition(Enchantment enchantment, int minLevel){
            return this.condition(MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(enchantment, MinMaxBounds.Ints.atLeast(minLevel)))).build());
        }

        /**
         * Adds a condition for the used tool to have the given enchantment.
         * @param enchantment enchantment required
         */
        public LootPoolBuilder hasEnchantmentCondition(Enchantment enchantment){
            return this.hasEnchantmentCondition(enchantment, 1);
        }

        /**
         * Adds an entry to this loot pool.
         * @param entry entry to be added
         */
        public LootPoolBuilder entry(LootPoolEntryContainer entry){
            if(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.getKey(entry.getType()) == null)
                throw new IllegalArgumentException("Cannot use unregistered loot pool entry '" + entry + "'!");

            this.entries.add(entry);
            return this;
        }

        private LootPoolBuilder entry(LootPoolSingletonContainer.Builder<?> entry, int weight){
            if(weight <= 0)
                throw new IllegalArgumentException("Loot entry weight must be greater than zero, not '" + weight + "'!");

            return this.entry(entry.setWeight(weight).build());
        }

        /**
         * Adds an empty entry to this loot pool.
         * @param weight weight of the entry
         */
        public LootPoolBuilder emptyEntry(int weight){
            return this.entry(EmptyLootItem.emptyItem(), weight);
        }

        /**
         * Adds an empty entry to this loot pool.
         */
        public LootPoolBuilder emptyEntry(){
            return this.emptyEntry(1);
        }

        /**
         * Adds an item entry to this loot pool.
         * @param item   item to be added as an entry
         * @param weight weight of the entry
         */
        public LootPoolBuilder itemEntry(ItemLike item, int weight){
            return this.entry(LootItem.lootTableItem(item), weight);
        }

        /**
         * Adds an item entry to this loot pool.
         * @param item item to be added as an entry
         */
        public LootPoolBuilder itemEntry(ItemLike item){
            return this.itemEntry(item, 1);
        }

        /**
         * Adds an item entry to this loot pool.
         * @param item   item to be added as an entry
         * @param count  the number of items in the item stack
         * @param weight weight of the entry
         */
        public LootPoolBuilder itemEntry(ItemLike item, int count, int weight){
            return this.entry(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))), weight);
        }

        /**
         * Adds an item entry to this loot pool.
         * @param item   item to be added as an entry
         * @param min    the minimum size of the item stack
         * @param max    the maximum size of the item stack
         * @param weight weight of the entry
         */
        public LootPoolBuilder itemEntry(ItemLike item, int min, int max, int weight){
            return this.entry(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))), weight);
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
         * Adds an item entry which will be enchanted.
         * @param item        item to be enchanted
         * @param levels      the number of levels the item will be enchanted with
         * @param allowCurses whether the items may be enchanted with curses
         * @param weight      weight of the entry
         */
        public LootPoolBuilder enchantedItemEntry(ItemLike item, int levels, boolean allowCurses, int weight){
            EnchantWithLevelsFunction.Builder builder = EnchantWithLevelsFunction.enchantWithLevels(ConstantValue.exactly(levels));
            if(allowCurses)
                builder.allowTreasure();
            return this.entry(LootItem.lootTableItem(item).apply(builder), weight);
        }

        /**
         * Adds an item entry which will be enchanted.
         * @param item        item to be enchanted
         * @param minLevels   the minimum number of levels the item will be enchanted with
         * @param maxLevels   the maximum number of levels the item will be enchanted with
         * @param allowCurses whether the items may be enchanted with curses
         * @param weight      weight of the entry
         */
        public LootPoolBuilder enchantedItemEntry(ItemLike item, int minLevels, int maxLevels, boolean allowCurses, int weight){
            EnchantWithLevelsFunction.Builder builder = EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(minLevels, maxLevels));
            if(allowCurses)
                builder.allowTreasure();
            return this.entry(LootItem.lootTableItem(item).apply(builder), weight);
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param tagKey tag to be added as an entry
         * @param weight weight of the entry
         */
        public LootPoolBuilder tagEntry(TagKey<Item> tagKey, int weight){
            return this.entry(TagEntry.tagContents(tagKey), weight);
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param tagKey tag to be added as an entry
         */
        public LootPoolBuilder tagEntry(TagKey<Item> tagKey){
            return this.tagEntry(tagKey, 1);
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param tag    tag to be added as an entry
         * @param weight weight of the entry
         */
        public LootPoolBuilder tagEntry(ResourceLocation tag, int weight){
            return this.tagEntry(TagKey.create(Registries.ITEMS.getVanillaRegistry().key(), tag), weight);
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param tag tag to be added as an entry
         */
        public LootPoolBuilder tagEntry(ResourceLocation tag){
            return this.tagEntry(TagKey.create(Registries.ITEMS.getForgeRegistry().getRegistryKey(), tag));
        }

        /**
         * Adds a tag entry to this loot pool.
         * @param namespace namespace of the tag to be added as an entry
         * @param path      path of the tag to be added as an entry
         * @param weight    weight of the entry
         */
        public LootPoolBuilder tagEntry(String namespace, String path, int weight){
            return this.tagEntry(new ResourceLocation(namespace, path), weight);
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
         * @param weight    weight of the entry
         */
        public LootPoolBuilder lootTableEntry(ResourceLocation lootTable, int weight){
            return this.entry(LootTableReference.lootTableReference(lootTable), weight);
        }

        /**
         * Adds a loot table entry to this loot pool.
         * @param lootTable loot table to be added as an entry
         */
        public LootPoolBuilder lootTableEntry(ResourceLocation lootTable){
            return this.lootTableEntry(lootTable, 1);
        }

        /**
         * Adds a loot table entry to this loot pool.
         * @param namespace namespace of the loot table to be added as an entry
         * @param path      path of the loot table to be added as an entry
         * @param weight    weight of the entry
         */
        public LootPoolBuilder lootTableEntry(String namespace, String path, int weight){
            return this.lootTableEntry(new ResourceLocation(namespace, path), weight);
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
        public LootPoolBuilder function(LootItemFunction function){
            if(BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType()) == null)
                throw new IllegalArgumentException("Cannot use unregistered item function '" + function + "'!");

            this.functions.add(function);
            return this;
        }
    }
}
