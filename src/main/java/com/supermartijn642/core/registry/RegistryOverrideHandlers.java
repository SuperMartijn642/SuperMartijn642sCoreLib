package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created 16/06/2023 by SuperMartijn642
 */
@ApiStatus.Internal
public class RegistryOverrideHandlers {

    // Yes this is quite slow, but the values in the fields can't be cached as other mods could change them at any time

    // Used to generate access widener entries
    public static final List<Pair<Class<?>,Class<?>>> REQUESTED_FIELDS = new ArrayList<>();

    public static final BiConsumer<Object,Object> BLOCKS = overrideFields(Blocks.class, Block.class);
    public static final BiConsumer<Object,Object> FLUIDS = overrideFields(Fluids.class, Fluid.class);
    public static final BiConsumer<Object,Object> ITEMS = overrideFields(Items.class, Item.class).andThen(
        (oldValue, newValue) -> {
            // Replace entries in the block->item map
            Item.BY_BLOCK.entrySet().stream()
                .filter(entry -> entry.getValue() == oldValue)
                .toList()
                .forEach(entry -> Item.BY_BLOCK.put(entry.getKey(), (Item)newValue));
        }
    );
    public static final BiConsumer<Object,Object> MOB_EFFECTS = overrideFields(MobEffects.class, MobEffect.class);
    public static final BiConsumer<Object,Object> SOUND_EVENTS = overrideFields(SoundEvents.class, SoundEvent.class);
    public static final BiConsumer<Object,Object> POTIONS = overrideFields(Potions.class, Potion.class);
    public static final BiConsumer<Object,Object> ENCHANTMENTS = overrideFields(Enchantments.class, Enchantment.class);
    public static final BiConsumer<Object,Object> ENTITY_TYPES = overrideFields(EntityType.class, EntityType.class);
    public static final BiConsumer<Object,Object> BLOCK_ENTITY_TYPES = overrideFields(BlockEntityType.class, BlockEntityType.class);
    public static final BiConsumer<Object,Object> PARTICLE_TYPES = overrideFields(ParticleTypes.class, ParticleType.class);
    public static final BiConsumer<Object,Object> MENU_TYPES = overrideFields(MenuType.class, MenuType.class);
    public static final BiConsumer<Object,Object> RECIPE_TYPES = overrideFields(RecipeType.class, RecipeType.class);
    public static final BiConsumer<Object,Object> RECIPE_SERIALIZERS = overrideFields(RecipeSerializer.class, RecipeSerializer.class);
    public static final BiConsumer<Object,Object> ATTRIBUTES = overrideFields(Attributes.class, Attribute.class);
    public static final BiConsumer<Object,Object> STAT_TYPES = overrideFields(Stats.class, StatType.class);
    public static final BiConsumer<Object,Object> DATA_COMPONENT_TYPES = overrideFields(DataComponents.class, DataComponentType.class);

    private static BiConsumer<Object,Object> overrideFields(Class<?> clazz, Class<?> fieldType){
        REQUESTED_FIELDS.add(Pair.of(clazz, fieldType));
        Supplier<Field[]> fields = findFieldsInClass(clazz, fieldType, false);
        return (oldValue, newValue) -> replaceValueInFields(fields.get(), oldValue, newValue);
    }

    public static Supplier<Field[]> findFieldsInClass(Class<?> clazz, Class<?> fieldType, boolean allowFinal){
        Holder<Field[]> fieldsHolder = new Holder<>();
        return () -> {
            if(fieldsHolder.get() == null){
                List<Field> fields = new ArrayList<>();
                for(Field field : clazz.getDeclaredFields()){
                    if(Modifier.isStatic(field.getModifiers()) && (allowFinal || !Modifier.isFinal(field.getModifiers())) && fieldType.isAssignableFrom(field.getType()))
                        fields.add(field);
                }
                fieldsHolder.set(fields.toArray(Field[]::new));
            }
            return fieldsHolder.get();
        };
    }

    private static void replaceValueInFields(Field[] fields, Object oldValue, Object newValue){
        try{
            for(Field field : fields){
                field.setAccessible(true);
                if(field.get(null) == oldValue){
                    if(field.getType().isAssignableFrom(newValue.getClass()))
                        field.set(null, newValue);
                    else
                        CoreLib.LOGGER.warn("Could not override field '" + field + "' of type '" + field.getType() + "' with value '" + newValue + "' of type '" + newValue.getClass() + "'!");
                }
            }
        }catch(IllegalAccessException e){
            CoreLib.LOGGER.error("Encountered an exception whilst applying registry overrides in a vanilla class! Vanilla fields will not be updated!", e);
        }
    }
}
