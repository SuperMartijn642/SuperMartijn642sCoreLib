package com.supermartijn642.core.mixin;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.DatagenModLoader;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created 07/05/2023 by SuperMartijn642
 */
@Mixin(value = DatagenModLoader.class, remap = false)
public interface DatagenModLoaderAccessor {

    @Accessor(value = "dataGeneratorConfig", remap = false)
    static GatherDataEvent.DataGeneratorConfig getDataGeneratorConfig(){
        throw new AssertionError();
    }

    @Accessor(value = "existingFileHelper", remap = false)
    static ExistingFileHelper getExistingFileHelper(){
        throw new AssertionError();
    }
}
