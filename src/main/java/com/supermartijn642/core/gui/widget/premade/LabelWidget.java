package com.supermartijn642.core.gui.widget.premade;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public class LabelWidget extends BaseWidget {

    private final Supplier<Component> text;
    private boolean active = true;

    /**
     * @param text the text to be displayed on the label
     */
    public LabelWidget(int x, int y, int width, int height, Supplier<Component> text){
        super(x, y, width, height);
        this.text = text;
    }

    /**
     * @param text the text to be displayed on the label
     */
    public LabelWidget(int x, int y, int width, int height, Component text){
        this(x, y, width, height, () -> text);
    }

    public void setActive(boolean active){
        this.active = active;
    }

    @Override
    public Component getNarrationMessage(){
        return this.text.get();
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(this.active){
            graphics.submitRectangle(this.x, this.y, this.width, this.height, p -> p.color(-6250336));
            graphics.submitRectangle(this.x + 1, this.y + 1, this.width - 2, this.height - 2, p -> p.color(0xff404040));

            graphics.submitText(this.text.get(), this.x + this.width / 2f, this.y + 2, p -> {
                p.shadow().centerHorizontally();
                if(this.active)
                    p.activeColor();
                else
                    p.inactiveColor();
            });
        }
    }
}
