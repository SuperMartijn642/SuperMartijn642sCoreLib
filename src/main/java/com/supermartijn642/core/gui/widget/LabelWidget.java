package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public class LabelWidget extends Widget {

    private final Supplier<Component> text;

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

    @Override
    protected Component getNarrationMessage(){
        return this.text.get();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks){
        if(this.active){
            ScreenUtils.fillRect(poseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
            ScreenUtils.fillRect(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xff404040);

            ScreenUtils.drawCenteredStringWithShadow(poseStack, text.get(), this.x, this.y + 2, this.active ? ScreenUtils.ACTIVE_TEXT_COLOR : ScreenUtils.INACTIVE_TEXT_COLOR);
        }
    }
}
