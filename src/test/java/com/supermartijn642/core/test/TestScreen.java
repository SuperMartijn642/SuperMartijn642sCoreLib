package com.supermartijn642.core.test;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import net.minecraft.network.chat.Component;

/**
 * Created 1/22/2021 by SuperMartijn642
 */
public class TestScreen extends BaseWidget {

    protected TestScreen(){
        super(0, 0, 100, 100);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.string("Test Screen").get();
    }

    @Override
    protected void addWidgets(){
        TextFieldWidget textField = this.addWidget(new TextFieldWidget(10, 10, 50, 20, "Default", 50));
        textField.setSuggestion("Suggestion");
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitDefaultScreenBackground(this.x, this.y, this.width, this.height);
        super.renderBackground(context, graphics, mouseX, mouseY);
    }
}
