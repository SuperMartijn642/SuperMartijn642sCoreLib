package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.BlockTagGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public class CoreLibMiningTagGenerator extends BlockTagGenerator {

    public CoreLibMiningTagGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        // Cause all these tags to generate
        this.mineableWithAxe();
        this.mineableWithHoe();
        this.mineableWithPickaxe();
        this.mineableWithShovel();
        this.needsDiamondTool();
        this.needsIronTool();
        this.needsStoneTool();
    }
}
