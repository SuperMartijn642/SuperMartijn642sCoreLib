package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import net.minecraft.tags.BlockTags;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public class CoreLibMiningTagGenerator extends TagGenerator {

    public CoreLibMiningTagGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        // Cause all these tags to generate
        this.blockTag(BlockTags.MINEABLE_WITH_AXE);
        this.blockTag(BlockTags.MINEABLE_WITH_HOE);
        this.blockTag(BlockTags.MINEABLE_WITH_PICKAXE);
        this.blockTag(BlockTags.MINEABLE_WITH_SHOVEL);
        this.blockTag(BlockTags.NEEDS_DIAMOND_TOOL);
        this.blockTag(BlockTags.NEEDS_IRON_TOOL);
        this.blockTag(BlockTags.NEEDS_STONE_TOOL);
    }
}
