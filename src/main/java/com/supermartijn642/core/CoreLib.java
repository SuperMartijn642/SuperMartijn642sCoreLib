package com.supermartijn642.core;

import com.supermartijn642.core.data.condition.*;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.generator.standard.CoreLibMiningTagGenerator;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642corelib")
public class CoreLib {

    public static final Logger LOGGER = LoggerFactory.getLogger("supermartijn642corelib");

    public CoreLib(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConstructMod);
        CommonUtils.initialize();

        // Register conditional recipe type
        RegistrationHandler handler = RegistrationHandler.get("supermartijn642corelib");
        handler.registerRecipeSerializer("conditional", ConditionalRecipeSerializer.INSTANCE);
        handler.registerResourceConditionSerializer("mod_loaded", ModLoadedResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("not", NotResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("or", OrResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("and", AndResourceCondition.SERIALIZER);
        handler.registerResourceConditionSerializer("tag_populated", TagPopulatedResourceCondition.SERIALIZER);

        // Register generator for default tags
        GeneratorRegistrationHandler.get("supermartijn642corelib").addGenerator(cache -> new CoreLibMiningTagGenerator("supermartijn642corelib", cache));

        // Add all BaseItem instances to their respective creative tabs

        FMLJavaModLoadingContext.get().getModEventBus().addListener((Consumer<BuildCreativeModeTabContentsEvent>)event -> {
            Registries.ITEMS.getValues().stream()
                .filter(item -> item instanceof BaseItem || item instanceof BaseBlockItem)
                .filter(item -> item instanceof BaseItem ? ((BaseItem)item).isInCreativeGroup(event.getTab()) : ((BaseBlockItem)item).isInCreativeGroup(event.getTab()))
                .forEach(event::accept);
        });
    }

    private void onConstructMod(FMLConstructModEvent e){
        RegistryEntryAcceptor.Handler.gatherAnnotatedFields();
    }
}
