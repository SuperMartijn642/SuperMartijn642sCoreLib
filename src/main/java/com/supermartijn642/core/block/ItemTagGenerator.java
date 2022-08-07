package com.supermartijn642.core.block;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.item.Item;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public abstract class ItemTagGenerator extends TagGenerator<Item> {

    public ItemTagGenerator(String modid, ResourceCache cache){
        super(modid, cache, "items", Registries.ITEMS);
    }
}
