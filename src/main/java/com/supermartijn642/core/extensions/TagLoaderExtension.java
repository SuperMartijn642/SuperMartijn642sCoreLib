package com.supermartijn642.core.extensions;

import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public interface TagLoaderExtension {

    void supermartijn642corelibSetRegistry(Registry<?> registry, ForgeRegistry<?> forgeRegistry);
}
