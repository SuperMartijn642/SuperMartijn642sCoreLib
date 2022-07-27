package com.supermartijn642.core.gui.widget.premade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.network.chat.Component;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class ButtonWidget extends AbstractButtonWidget {

    private Component text;
    private boolean active = true;

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

    public void setActive(boolean active){
        this.active = active;
    }

    @Override
    public Component getNarrationMessage(){
        return this.text;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY){
        ScreenUtils.drawButtonBackground(poseStack, this.x, this.y, this.width, this.height, (this.active ? this.isFocused() ? 5 : 0 : 10) / 15f);
        ScreenUtils.drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), this.text, this.x + this.width / 2f, this.y + this.height / 2f - 5, this.active ? 0xFFFFFFFF : Integer.MAX_VALUE);
    }
}
