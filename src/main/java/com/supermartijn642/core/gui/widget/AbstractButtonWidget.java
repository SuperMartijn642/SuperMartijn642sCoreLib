package com.supermartijn642.core.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class AbstractButtonWidget extends Widget {

    private final Runnable pressable;

    public AbstractButtonWidget(int x, int y, int width, int height, Runnable onPress){
        super(x, y, width, height);
        this.pressable = onPress;
    }

    public void onPress(){
        playClickSound();
        if(this.pressable != null)
            this.pressable.run();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button){
        if(this.active && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height)
            this.onPress();
    }

    public static void playClickSound(){
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
