package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.gui.GuiGraphicsHelper;

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
            .texture(GuiGraphicsHelper.SCREEN_BACKGROUND_SPRITE)
            .texture(GuiGraphicsHelper.BUTTON_DEFAULT_SPRITE)
            .texture(GuiGraphicsHelper.BUTTON_HIGHLIGHTED_SPRITE)
            .texture(GuiGraphicsHelper.BUTTON_DISABLED_SPRITE)
            .texture(GuiGraphicsHelper.SLOT_SPRITE);
    }
}
