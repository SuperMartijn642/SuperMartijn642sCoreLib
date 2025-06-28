package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 28/06/2025 by SuperMartijn642
 */
public class CoreLibAtlasSourceGenerator extends AtlasSourceGenerator {

    public CoreLibAtlasSourceGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        this.guiAtlas()
            .texture("gui/background")
            .texture("gui/buttons")
            .texture("gui/slot");
    }
}
