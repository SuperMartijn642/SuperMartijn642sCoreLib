package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.GuiGraphicsExtractorExtension;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Created 28/06/2025 by SuperMartijn642
 */
@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin implements GuiGraphicsExtractorExtension {

    @Unique
    private GuiGraphicsHelper helper;

    @Override
    public GuiGraphicsHelper supermartijn642corelibGetHelper(){
        return this.helper;
    }

    @Override
    public void supermartijn642corelibSetHelper(GuiGraphicsHelper helper){
        this.helper = helper;
    }
}
