package com.supermartijn642.core.gui.widget;

import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public class LabelWidget extends Widget {

    private final Supplier<ITextComponent> text;

    /**
     * @param text the text to be displayed on the label
     */
    public LabelWidget(int x, int y, int width, int height, Supplier<ITextComponent> text){
        super(x, y, width, height);
        this.text = text;
    }

    /**
     * @param text the text to be displayed on the label
     */
    public LabelWidget(int x, int y, int width, int height, ITextComponent text){
        this(x, y, width, height, () -> text);
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.text.get();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        if(this.active){
            ScreenUtils.fillRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
            ScreenUtils.fillRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xff404040);

            ITextComponent text = this.text.get();
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            ScreenUtils.drawCenteredStringWithShadow(font, text, this.x, this.y + 2, this.active ? ScreenUtils.ACTIVE_TEXT_COLOR : ScreenUtils.INACTIVE_TEXT_COLOR);
        }
    }
}
