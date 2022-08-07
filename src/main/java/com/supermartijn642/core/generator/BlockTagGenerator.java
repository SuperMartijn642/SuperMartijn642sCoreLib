package com.supermartijn642.core.generator;

import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public abstract class BlockTagGenerator extends TagGenerator<Block> {

    public BlockTagGenerator(String modid, ResourceCache cache){
        super(modid, cache, "blocks", Registries.BLOCKS);
    }

    protected TagBuilder mineableWithAxe(){
        return this.tag(new ResourceLocation("minecraft", "mineable/axe"));
    }

    protected TagBuilder mineableWithHoe(){
        return this.tag(new ResourceLocation("minecraft", "mineable/hoe"));
    }

    protected TagBuilder mineableWithPickaxe(){
        return this.tag(new ResourceLocation("minecraft", "mineable/pickaxe"));
    }

    protected TagBuilder mineableWithShovel(){
        return this.tag(new ResourceLocation("minecraft", "mineable/shovel"));
    }

    protected TagBuilder needsDiamondTool(){
        return this.tag(new ResourceLocation("minecraft", "needs_diamond_tool"));
    }

    protected TagBuilder needsIronTool(){
        return this.tag(new ResourceLocation("minecraft", "needs_iron_tool"));
    }

    protected TagBuilder needsStoneTool(){
        return this.tag(new ResourceLocation("minecraft", "needs_stone_tool"));
    }
}
