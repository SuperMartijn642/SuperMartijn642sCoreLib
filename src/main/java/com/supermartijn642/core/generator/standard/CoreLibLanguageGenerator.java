package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 10/01/2026 by SuperMartijn642
 */
public class CoreLibLanguageGenerator extends LanguageGenerator {

    public CoreLibLanguageGenerator(String modid, ResourceCache cache){
        super(modid, cache, "en_us");
    }

    @Override
    public void generate(){
        this.translation("supermartijn642corelib.widgets.scrollbar.narration", "scroll bar");
    }
}
