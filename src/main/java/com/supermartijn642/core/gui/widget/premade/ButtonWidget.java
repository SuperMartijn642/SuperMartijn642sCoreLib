package com.supermartijn642.core.gui.widget.premade;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import net.minecraft.network.chat.Component;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class ButtonWidget extends AbstractButtonWidget {

    private Component text;

    /**
     * @param text    the text to be displayed on the button
     * @param onPress the action which will called when the user clicks the
     *                widget
     */
    public ButtonWidget(int x, int y, int width, int height, Component text, Runnable onPress){
        super(x, y, width, height, onPress);
        this.text = text;
    }

    /**
     * Sets the text which is displayed on the button.
     */
    public void setText(Component text){
        this.text = text;
    }

    public Component getText(){
        return this.text;
    }

    @Override
    public Component getNarrationMessage(){
        return this.text;
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitDefaultButton(
            this.isActive() ? this.isFocused() ? GuiGraphicsHelper.ButtonState.HIGHLIGHTED : GuiGraphicsHelper.ButtonState.DEFAULT : GuiGraphicsHelper.ButtonState.DISABLED,
            this.x, this.y, this.width, this.height
        );
        graphics.submitText(this.text, this.x + this.width / 2f, this.y + this.height / 2f - 5, p -> p.shadow().centerHorizontally().color(this.isActive() ? 0xFFFFFFFF : Integer.MAX_VALUE));
    }
}
