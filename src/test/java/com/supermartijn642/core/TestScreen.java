package com.supermartijn642.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.core.gui.widget.TextFieldWidget;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
public class TestScreen extends BaseScreen {

    protected TestScreen(){
        super(TextComponents.string("Test Screen").get());
    }

    @Override
    protected float sizeX(){
        return 100;
    }

    @Override
    protected float sizeY(){
        return 100;
    }

    @Override
    protected void addWidgets(){
        TextFieldWidget textField = this.addWidget(new TextFieldWidget(10, 10, 50, 20, "Default", 50));
        textField.setSuggestion("Suggestion");
    }

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        this.drawScreenBackground(matrixStack);
    }
}
