package com.supermartijn642.core.gui.widget.premade;

import com.supermartijn642.core.gui.ScreenUtils;
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
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        if(this.active){
            ScreenUtils.fillRect(context.poseStack(), this.x, this.y, this.width, this.height, -6250336);
            ScreenUtils.fillRect(context.poseStack(), this.x + 1, this.y + 1, this.width - 2, this.height - 2, 0xff404040);

            ScreenUtils.drawCenteredStringWithShadow(context.poseStack(), this.text.get(), this.x + this.width / 2f, this.y + 2, this.active ? ScreenUtils.ACTIVE_TEXT_COLOR : ScreenUtils.INACTIVE_TEXT_COLOR);
        }
    }
}
