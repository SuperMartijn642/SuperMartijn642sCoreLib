package com.supermartijn642.core;

import com.google.common.collect.ImmutableMap;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.data.condition.*;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.data.tag.CustomTagEntries;
import com.supermartijn642.core.data.tag.entries.NamespaceTagEntry;
import com.supermartijn642.core.generator.standard.CoreLibLanguageGenerator;
import com.supermartijn642.core.generator.standard.CoreLibMiningTagGenerator;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642corelib")
public class CoreLib {

    public static final Logger LOGGER = CommonUtils.getLogger("supermartijn642corelib");

    public CoreLib(FMLJavaModLoadingContext context){
        context.getModEventBus().addListener(this::onConstructMod);
        CommonUtils.initialize();
        CustomTagEntries.init();

        // Register conditional recipe type
        RegistrationHandler handler = RegistrationHandler.get("supermartijn642corelib");
        handler.registerRecipeType("dummy", ConditionalRecipeSerializer.DUMMY_RECIPE_TYPE);
        handler.registerRecipeSerializer("conditional", ConditionalRecipeSerializer.INSTANCE);
        handler.registerResourceConditionSerializer("mod_loaded", ModLoadedResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("not", NotResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("or", OrResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("and", AndResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("tag_populated", TagPopulatedResourceCondition.SERIALIZER);

        // Register custom tag entry types
        handler.registerCustomTagEntrySerializer("namespace", NamespaceTagEntry.SERIALIZER);

        // Register base block tile data component
        handler.registerDataComponentType("tile_data", BaseBlock.TILE_DATA);

        // Register generator for default tags
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibMiningTagGenerator("supermartijn642corelib", cache));
        // Register generator for translations
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibLanguageGenerator("supermartijn642corelib", cache));

        // Add all BaseItem instances to their respective creative tabs
        context.getModEventBus().addListener((Consumer<BuildCreativeModeTabContentsEvent>)event -> {
            finalizeItemsPerCreativeGroup();
            itemsPerCreativeGroup.getOrDefault(event.getTab(), List.of()).forEach(event::accept);
        });
    }

    private void onConstructMod(FMLConstructModEvent e){
        RegistryEntryAcceptor.Handler.gatherAnnotatedFields();
    }

    private static Map<CreativeModeTab,Collection<Item>> itemsPerCreativeGroup = new HashMap<>();
    private static boolean creativeGroupsInitialized = false;

    public static void addItemToCreativeGroup(CreativeModeTab group, Item item){
        if(creativeGroupsInitialized)
            throw new IllegalStateException("Creative mode tabs have already been initialized!");
        itemsPerCreativeGroup.computeIfAbsent(group, o -> new HashSet<>()).add(item);
    }

    private static void finalizeItemsPerCreativeGroup(){
        if(creativeGroupsInitialized)
            return;
        creativeGroupsInitialized = true;
        ImmutableMap.Builder<CreativeModeTab,Collection<Item>> builder = ImmutableMap.builder();
        itemsPerCreativeGroup.forEach((group, items) -> {
            List<Item> sorted = new ArrayList<>(items);
            sorted.sort(Comparator.comparing(Registries.ITEMS::getIdentifier));
            builder.put(group, List.copyOf(sorted));
        });
        itemsPerCreativeGroup = builder.build();
    }
}
