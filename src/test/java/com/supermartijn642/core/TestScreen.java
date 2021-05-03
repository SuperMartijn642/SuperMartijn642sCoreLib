package com.supermartijn642.core;

import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
public class TestScreen extends BaseScreen {
    protected TestScreen(){
        super(new StringTextComponent("Test Screen"));
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
    protected void render(int mouseX, int mouseY){
        this.drawScreenBackground();
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY){

    }
}