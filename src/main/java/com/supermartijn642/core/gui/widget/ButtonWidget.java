package com.supermartijn642.core.gui.widget;

import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class ButtonWidget extends AbstractButtonWidget {

    private ITextComponent text;

    public ButtonWidget(int x, int y, int width, int height, ITextComponent text, Runnable onPress){
        super(x, y, width, height, onPress);
        this.text = text;
    }

    public void setText(ITextComponent text){
        this.text = text;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.text;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        ScreenUtils.drawButtonBackground(this.x, this.y, this.width, this.height, (this.active ? this.isHovered() ? 5 : 0 : 10) / 15f);
        ScreenUtils.drawCenteredStringWithShadow(Minecraft.getInstance().fontRenderer, this.text, this.x + this.width / 2f, this.y + this.height / 2f - 5, this.active ? 0xFFFFFFFF : Integer.MAX_VALUE);
    }
}
