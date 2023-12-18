package com.supermartijn642.core.mixin.dev;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created 07/05/2023 by SuperMartijn642
 */
@Mixin(value = DatagenModLoader.class, remap = false)
public interface DatagenModLoaderAccessor {

    @Accessor(value = "existingFileHelper", remap = false)
    static ExistingFileHelper getExistingFileHelper(){
        throw new AssertionError();
    }
}
