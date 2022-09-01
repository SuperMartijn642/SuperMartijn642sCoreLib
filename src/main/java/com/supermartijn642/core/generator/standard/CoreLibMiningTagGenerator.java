package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import net.minecraft.util.ResourceLocation;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public class CoreLibMiningTagGenerator extends TagGenerator {

    private static final ResourceLocation MINEABLE_WITH_AXE = new ResourceLocation("mineable/axe");
    private static final ResourceLocation MINEABLE_WITH_HOE = new ResourceLocation("mineable/hoe");
    private static final ResourceLocation MINEABLE_WITH_PICKAXE = new ResourceLocation("mineable/pickaxe");
    private static final ResourceLocation MINEABLE_WITH_SHOVEL = new ResourceLocation("mineable/shovel");
    private static final ResourceLocation NEEDS_DIAMOND_TOOL = new ResourceLocation("needs_diamond_tool");
    private static final ResourceLocation NEEDS_IRON_TOOL = new ResourceLocation("needs_iron_tool");
    private static final ResourceLocation NEEDS_STONE_TOOL = new ResourceLocation("needs_stone_tool");

    public CoreLibMiningTagGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        // Cause all these tags to generate
        this.blockTag(MINEABLE_WITH_AXE);
        this.blockTag(MINEABLE_WITH_HOE);
        this.blockTag(MINEABLE_WITH_PICKAXE);
        this.blockTag(MINEABLE_WITH_SHOVEL);
        this.blockTag(NEEDS_DIAMOND_TOOL);
        this.blockTag(NEEDS_IRON_TOOL);
        this.blockTag(NEEDS_STONE_TOOL);
    }
}
