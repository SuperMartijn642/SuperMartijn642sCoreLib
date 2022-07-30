package com.supermartijn642.core.test;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
public class TestScreen extends BaseWidget {

    protected TestScreen(){
        super(0, 0, 100, 100);
    }

    @Override
    public ITextComponent getNarrationMessage(){
        return TextComponents.string("Test Screen").get();
    }

    @Override
    protected void addWidgets(){
        TextFieldWidget textField = this.addWidget(new TextFieldWidget(10, 10, 50, 20, "Default", 50));
        textField.setSuggestion("Suggestion");
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY){
        ScreenUtils.drawScreenBackground(poseStack, this.x, this.y, this.width, this.height);
        super.renderBackground(poseStack, mouseX, mouseY);
    }
}
