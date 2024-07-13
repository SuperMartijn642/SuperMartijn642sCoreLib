package com.supermartijn642.core.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.core.data.condition.ModLoadedResourceCondition;
import com.supermartijn642.core.data.condition.NotResourceCondition;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.util.Pair;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.*;

/**
 * Created 22/08/2022 by SuperMartijn642
 */
public abstract class AdvancementGenerator extends ResourceGenerator {

    private final Map<ResourceLocation,AdvancementBuilder> advancements = new HashMap<>();

    public AdvancementGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        DynamicOps<JsonElement> ops = ResourceGenerator.registryAccess.createSerializationContext(JsonOps.INSTANCE);
        // Loop over all advancements
        for(AdvancementBuilder advancementBuilder : this.advancements.values()){
            // Verify the advancement has any criteria
            if(advancementBuilder.criteria.isEmpty())
                throw new RuntimeException("Advancement '" + advancementBuilder.identifier + "' does not have any criteria!");
            // Verify all requirements
            if(advancementBuilder.requirements.isEmpty()){
                if(advancementBuilder.criteria.size() != 1)
                    throw new RuntimeException("Advancement '" + advancementBuilder.identifier + "' does not have any requirements set!");
                advancementBuilder.requirements.add(advancementBuilder.criteria.keySet().toArray(String[]::new));
            }
            for(String criterion : advancementBuilder.requirements.stream().flatMap(Arrays::stream).toArray(String[]::new)){
                if(advancementBuilder.criteria.containsKey(criterion))
                    continue;
                throw new RuntimeException("Found requirement for unknown criterion '" + criterion + "' in advancement '" + advancementBuilder.identifier + "'!");
            }

            JsonObject json = new JsonObject();
            // Conditions
            if(!advancementBuilder.conditions.isEmpty()){
                JsonArray conditionsJson = new JsonArray();
                for(ResourceCondition condition : advancementBuilder.conditions){
                    JsonObject conditionJson = new JsonObject();
                    conditionJson.addProperty("condition", Registries.RESOURCE_CONDITION_SERIALIZERS.getIdentifier(condition.getSerializer()).toString());
                    //noinspection unchecked,rawtypes
                    ((ResourceConditionSerializer)condition.getSerializer()).serialize(conditionJson, condition);
                    conditionsJson.add(conditionJson);
                }
                json.add("fabric:load_conditions", conditionsJson);
            }
            // Parent
            if(advancementBuilder.parent != null){
                ResourceLocation parent = advancementBuilder.parent;
                if(!this.advancements.containsKey(parent) && !this.cache.doesResourceExist(ResourceType.DATA, parent.getNamespace(), "advancement", parent.getPath(), ".json"))
                    throw new RuntimeException("Could not find parent '" + parent + "' for advancement '" + advancementBuilder.identifier + "'!");
                json.addProperty("parent", parent.toString());
            }
            // Display
            JsonObject displayJson = new JsonObject();
            // Icon
            if(advancementBuilder.icon == null && !ResourceLocation.fromNamespaceAndPath("minecraft", "recipes/root").equals(advancementBuilder.parent))
                throw new RuntimeException("Advancement '" + advancementBuilder.identifier + "' must have an icon!");
            if(advancementBuilder.icon != null){
                JsonObject iconJson = new JsonObject();
                iconJson.addProperty("id", Registries.ITEMS.getIdentifier(advancementBuilder.icon).toString());
                if(advancementBuilder.iconComponents != null && !advancementBuilder.iconComponents.isEmpty())
                    iconJson.add("components", DataComponentPatch.CODEC.encodeStart(ops, advancementBuilder.iconComponents).getOrThrow());
                displayJson.add("icon", iconJson);
            }
            // Title
            JsonObject titleJson = new JsonObject();
            titleJson.addProperty("translate", advancementBuilder.titleKey);
            displayJson.add("title", titleJson);
            // Description
            JsonObject description = new JsonObject();
            description.addProperty("translate", advancementBuilder.descriptionKey);
            displayJson.add("description", description);
            // Frame
            displayJson.addProperty("frame", advancementBuilder.frame.getSerializedName());
            // Background
            if(advancementBuilder.background != null){
                if(!this.cache.doesResourceExist(ResourceType.ASSET, advancementBuilder.background.getNamespace(), "textures", advancementBuilder.background.getPath(), ".png"))
                    throw new RuntimeException("Could not find background texture '" + advancementBuilder.background + "' for advancement '" + advancementBuilder.identifier + "'!");

                displayJson.addProperty("background", advancementBuilder.background.getNamespace() + ":textures/" + advancementBuilder.background.getPath() + ".png");
            }
            // Show toast
            displayJson.addProperty("show_toast", advancementBuilder.showToast);
            // Announce to chat
            displayJson.addProperty("announce_to_chat", advancementBuilder.announceToChat);
            // Hidden
            displayJson.addProperty("hidden", advancementBuilder.hidden);
            json.add("display", displayJson);
            // Criteria
            JsonObject criteriaJson = new JsonObject();
            for(Map.Entry<String,Pair<CriterionTrigger<?>,CriterionTriggerInstance>> criterion : advancementBuilder.criteria.entrySet()){
                JsonObject criterionJson = new JsonObject();
                criterionJson.addProperty("trigger", BuiltInRegistries.TRIGGER_TYPES.getKey(criterion.getValue().left()).toString());
                //noinspection unchecked
                JsonElement conditionsJson = ((Codec<CriterionTriggerInstance>)criterion.getValue().left().codec()).encodeStart(ops, criterion.getValue().right()).getOrThrow();
                if(!conditionsJson.isJsonObject() || !conditionsJson.getAsJsonObject().isEmpty())
                    criterionJson.add("conditions", conditionsJson);
                criteriaJson.add(criterion.getKey(), criterionJson);
            }
            json.add("criteria", criteriaJson);
            // Requirements
            JsonArray requirementsArray = new JsonArray();
            for(String[] requirementGroup : advancementBuilder.requirements){
                JsonArray groupArray = new JsonArray();
                Arrays.stream(requirementGroup).forEach(groupArray::add);
                requirementsArray.add(groupArray);
            }
            json.add("requirements", requirementsArray);
            // Rewards
            JsonObject rewardsJson = new JsonObject();
            // Recipe rewards
            if(!advancementBuilder.rewardRecipes.isEmpty()){
                JsonArray recipesJson = new JsonArray();
                for(ResourceLocation rewardRecipe : advancementBuilder.rewardRecipes){
                    if(!this.cache.doesResourceExist(ResourceType.DATA, rewardRecipe.getNamespace(), "recipe", rewardRecipe.getPath(), ".json"))
                        throw new RuntimeException("Could not find reward recipe '" + rewardRecipe + "' for advancement '" + advancementBuilder.identifier + "'!");

                    recipesJson.add(rewardRecipe.toString());
                }
                rewardsJson.add("recipes", recipesJson);
            }
            // Loot table rewards
            if(!advancementBuilder.rewardLootTables.isEmpty()){
                JsonArray lootTablesJson = new JsonArray();
                for(ResourceLocation rewardLootTable : advancementBuilder.rewardLootTables){
                    if(!this.cache.doesResourceExist(ResourceType.DATA, rewardLootTable.getNamespace(), "loot_tables", rewardLootTable.getPath(), ".json"))
                        throw new RuntimeException("Could not find reward loot table '" + rewardLootTable + "' for advancement '" + advancementBuilder.identifier + "'!");

                    lootTablesJson.add(rewardLootTable.toString());
                }
                rewardsJson.add("loot", lootTablesJson);
            }
            // Reward experience
            if(advancementBuilder.rewardExperience != 0)
                rewardsJson.addProperty("experience", advancementBuilder.rewardExperience);
            if(rewardsJson.size() != 0)
                json.add("rewards", rewardsJson);

            // Save the object to the cache
            ResourceLocation identifier = advancementBuilder.identifier;
            this.cache.saveJsonResource(ResourceType.DATA, json, identifier.getNamespace(), "advancement", identifier.getPath());
        }
    }

    /**
     * Creates a new advancement builder for the given identifier.
     * @param identifier location of the advancement
     */
    public AdvancementBuilder advancement(ResourceLocation identifier){
        if(this.advancements.containsKey(identifier))
            throw new RuntimeException("Duplicate advancement with identifier '" + identifier + "'!");

        this.cache.trackToBeGeneratedResource(ResourceType.DATA, identifier.getNamespace(), "advancement", identifier.getPath(), ".json");
        return this.advancements.computeIfAbsent(identifier, i -> new AdvancementBuilder(this.modid, i));
    }

    /**
     * Creates a new advancement builder for the given namespace and path.
     * @param namespace namespace of the advancement
     * @param path      path of the advancement
     */
    public AdvancementBuilder advancement(String namespace, String path){
        return this.advancement(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    /**
     * Creates a new advancement builder for the given namespace and path.
     * @param identifier location of the advancement
     */
    public AdvancementBuilder advancement(String identifier){
        return this.advancement(this.modid, identifier);
    }

    @Override
    public String getName(){
        return this.modName + " Advancement Generator";
    }

    protected static class AdvancementBuilder {

        protected final String modid;
        protected final ResourceLocation identifier;
        private final List<ResourceCondition> conditions = new ArrayList<>();
        private final Map<String,Pair<CriterionTrigger<?>,CriterionTriggerInstance>> criteria = new LinkedHashMap<>();
        private final List<String[]> requirements = new ArrayList<>();
        private final List<ResourceLocation> rewardLootTables = new ArrayList<>();
        private final List<ResourceLocation> rewardRecipes = new ArrayList<>();
        private ResourceLocation parent;
        private Item icon;
        private DataComponentPatch iconComponents;
        private String titleKey;
        private String descriptionKey;
        private AdvancementType frame = AdvancementType.TASK;
        private ResourceLocation background;
        private boolean showToast = true;
        private boolean announceToChat = true;
        private boolean hidden;
        private int rewardExperience;

        public AdvancementBuilder(String modid, ResourceLocation identifier){
            this.modid = modid;
            this.identifier = identifier;
            this.titleKey = identifier.getNamespace() + ".advancement." + identifier.getPath() + ".title";
            this.descriptionKey = identifier.getNamespace() + ".advancement." + identifier.getPath() + ".description";
        }

        /**
         * Adds a condition for this advancement to be loaded.
         */
        public AdvancementBuilder condition(ResourceCondition condition){
            this.conditions.add(condition);
            return this;
        }

        /**
         * Adds a condition to only load this advancement when the given condition is <b>not</b> satisfied.
         */
        public AdvancementBuilder notCondition(ResourceCondition condition){
            return this.condition(new NotResourceCondition(condition));
        }

        /**
         * Adds a condition to only load this advancement when a mod with the given modid is present.
         */
        public AdvancementBuilder modLoadedCondition(String modid){
            return this.condition(new ModLoadedResourceCondition(modid));
        }

        /**
         * Sets the parent advancement for this advancement.
         * @param advancement location of the parent advancement
         */
        public AdvancementBuilder parent(ResourceLocation advancement){
            if(this.identifier.equals(advancement))
                throw new IllegalArgumentException("Advancement '" + this.identifier + "' cannot have itself as parent!");

            this.parent = advancement;
            return this;
        }

        /**
         * Sets the parent advancement for this advancement.
         * @param namespace namespace of the parent advancement
         * @param path      path of the parent advancement
         */
        public AdvancementBuilder parent(String namespace, String path){
            return this.parent(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }

        /**
         * Sets the parent advancement for this advancement.
         * @param advancement location of the parent advancement
         */
        public AdvancementBuilder parent(String advancement){
            return this.parent(this.modid, advancement);
        }

        /**
         * Sets the icon for this advancement.
         * @param item       item to use as icon
         * @param components data components for the item
         */
        public AdvancementBuilder icon(ItemLike item, DataComponentPatch components){
            this.icon = item.asItem();
            this.iconComponents = components;
            return this;
        }

        /**
         * Sets the icon for this advancement.
         * @param item item to use as icon
         */
        public AdvancementBuilder icon(ItemLike item){
            return this.icon(item, null);
        }

        /**
         * Sets the icon for this advancement.
         * @param item identifier of the item to use as icon
         */
        public AdvancementBuilder icon(ResourceLocation item){
            if(!Registries.ITEMS.hasIdentifier(item))
                throw new IllegalArgumentException("Could not find any item registered under '" + item + "'!");

            return this.icon(Registries.ITEMS.getValue(item), null);
        }

        /**
         * Sets the icon for this advancement.
         * @param namespace  namespace of the item to use as icon
         * @param identifier path of the item to use as icon
         */
        public AdvancementBuilder icon(String namespace, String identifier){
            return this.icon(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
        }

        /**
         * Sets the translation key for the title of this advancement.
         * @param translationKey key to use for the title
         */
        public AdvancementBuilder title(String translationKey){
            if(translationKey == null || translationKey.isBlank())
                throw new IllegalArgumentException("Title translation key '" + translationKey + "' for advancement '" + this.identifier + "' must not be empty!");

            this.titleKey = translationKey;
            return this;
        }

        /**
         * Sets the translation key for the description of this advancement.
         * @param translationKey key to use for the description
         */
        public AdvancementBuilder description(String translationKey){
            if(translationKey == null || translationKey.isBlank())
                throw new IllegalArgumentException("Description translation key '" + translationKey + "' for advancement '" + this.identifier + "' must not be empty!");

            this.descriptionKey = translationKey;
            return this;
        }

        /**
         * Sets the frame type for this advancement.
         * @param frameType frame type to use
         */
        public AdvancementBuilder frame(AdvancementType frameType){
            this.frame = frameType;
            return this;
        }

        /**
         * Sets the challenge frame type for this advancement.
         */
        public AdvancementBuilder challengeFrame(){
            return this.frame(AdvancementType.CHALLENGE);
        }

        /**
         * Sets the goal frame type for this advancement.
         */
        public AdvancementBuilder goalFrame(){
            return this.frame(AdvancementType.GOAL);
        }

        /**
         * Sets the task frame type for this advancement.
         */
        public AdvancementBuilder taskFrame(){
            return this.frame(AdvancementType.TASK);
        }

        /**
         * Sets the background texture for this advancement. Only has effect if this advancement has no parent.
         * @param texture location of the background texture
         */
        public AdvancementBuilder background(ResourceLocation texture){
            this.background = texture;
            return this;
        }

        /**
         * Sets the background texture for this advancement. Only has effect if this advancement has no parent.
         * @param namespace namespace of the background texture
         * @param path      path of the background texture
         */
        public AdvancementBuilder background(String namespace, String path){
            return this.background(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }

        /**
         * Sets whether to show a toast when this advancement is obtained.
         * @param show whether to show a toast
         */
        public AdvancementBuilder showToast(boolean show){
            this.showToast = show;
            return this;
        }

        /**
         * Sets to not show a toast when this advancement is obtained.
         */
        public AdvancementBuilder dontShowToast(){
            return this.showToast(false);
        }

        /**
         * Sets whether to broadcast a chat message when this advancement is obtained.
         * @param announce whether to broadcast a chat message
         */
        public AdvancementBuilder announceToChat(boolean announce){
            this.announceToChat = announce;
            return this;
        }

        /**
         * Sets to not broadcast a chat message when this advancement is obtained.
         */
        public AdvancementBuilder dontAnnounceToChat(){
            return this.announceToChat(false);
        }

        /**
         * Sets whether this advancement is hidden.
         * @param hidden whether this advancement is hidden
         */
        public AdvancementBuilder hidden(boolean hidden){
            this.hidden = hidden;
            return this;
        }

        /**
         * Sets this advancement to be hidden.
         */
        public AdvancementBuilder hidden(){
            return this.hidden(true);
        }

        /**
         * Adds a criterion to this advancement. The criterion should be added to be requirements using the given name.
         * @param name      name for the criterion
         * @param criterion criterion to be added
         */
        public <T extends CriterionTriggerInstance> AdvancementBuilder criterion(String name, CriterionTrigger<T> criterion, T instance){
            if(this.criteria.containsKey(name))
                throw new RuntimeException("Duplicate criterion with name '" + name + "' for advancement '" + this.identifier + "'!");

            this.criteria.put(name, Pair.of(criterion, instance));
            return this;
        }

        /**
         * Adds a criterion to this advancement. The criterion should be added to be requirements using the given name.
         * @param name      name for the criterion
         * @param criterion criterion to be added
         */
        public AdvancementBuilder criterion(String name, Criterion<?> criterion){
            if(this.criteria.containsKey(name))
                throw new RuntimeException("Duplicate criterion with name '" + name + "' for advancement '" + this.identifier + "'!");

            this.criteria.put(name, Pair.of(criterion.trigger(), criterion.triggerInstance()));
            return this;
        }

        /**
         * Adds a criterion for the player to have the given items. The criterion should be added to be requirements using the given name.
         * @param name  name for the criterion
         * @param items items needed to satisfy the criterion
         */
        public AdvancementBuilder hasItemsCriterion(String name, ItemLike... items){
            this.criterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(items));
            return this;
        }

        /**
         * Adds a new group of requirements. The advancement will be obtained when any group is satisfied.
         * @param criteria criteria for the group
         */
        public AdvancementBuilder requirementGroup(String... criteria){
            if(this.requirements.contains(criteria))
                throw new RuntimeException("Duplicate requirement group '" + Arrays.toString(criteria) + "' for advancement '" + this.identifier + "'!");

            this.requirements.add(criteria);
            return this;
        }

        /**
         * Adds the given groups to requirements. The advancement will be obtained when any group is satisfied.
         * @param groups groups to be added
         */
        public AdvancementBuilder requirements(String[]... groups){
            Arrays.stream(groups).forEach(this::requirementGroup);
            return this;
        }

        /**
         * Sets the experience to be awarded when the advancement is obtained.
         * @param experience the amount of experience
         */
        public AdvancementBuilder rewardExperience(int experience){
            if(experience < 0)
                throw new IllegalArgumentException("Reward experience for advancement '" + this.identifier + "' must be greater than 0, not '" + experience + "'!");

            this.rewardExperience = experience;
            return this;
        }

        /**
         * Adds a loot table to be awarded when the advancement is obtained.
         * @param lootTable location of the loot table
         */
        public AdvancementBuilder rewardLootTable(ResourceLocation lootTable){
            this.rewardLootTables.add(lootTable);
            return this;
        }

        /**
         * Adds a loot table to be awarded when the advancement is obtained.
         * @param namespace namespace of the loot table
         * @param path      path of the loot table
         */
        public AdvancementBuilder rewardLootTable(String namespace, String path){
            return this.rewardLootTable(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }

        /**
         * Adds a recipe to be awarded when the advancement is obtained.
         * @param recipe location of the recipe
         */
        public AdvancementBuilder rewardRecipe(ResourceLocation recipe){
            if(this.rewardRecipes.contains(recipe))
                throw new RuntimeException("Duplicate recipe reward '" + recipe + "' for advancement '" + this.identifier + "'!");

            this.rewardRecipes.add(recipe);
            return this;
        }

        /**
         * Adds a recipe to be awarded when the advancement is obtained.
         * @param namespace namespace of the recipe
         * @param path      path of the recipe
         */
        public AdvancementBuilder rewardRecipe(String namespace, String path){
            return this.rewardRecipe(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }
}
