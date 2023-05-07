package com.supermartijn642.core.mixin;

import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created 07/05/2023 by SuperMartijn642
 */
@Mixin(value = ModLoader.class, remap = false)
public interface DatagenModLoaderAccessor {

    @Accessor(value = "dataGeneratorConfig", remap = false)
    GatherDataEvent.DataGeneratorConfig getDataGeneratorConfig();

    @Accessor(value = "existingFileHelper", remap = false)
    ExistingFileHelper getExistingFileHelper();
}
