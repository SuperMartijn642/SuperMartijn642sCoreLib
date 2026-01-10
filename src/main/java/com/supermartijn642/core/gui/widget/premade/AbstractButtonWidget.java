package com.supermartijn642.core.gui.widget.premade;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.CursorTypes;
import com.supermartijn642.core.gui.widget.BaseWidget;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class AbstractButtonWidget extends BaseWidget {

    private final Runnable pressable;

    /**
     * @param onPress the action which will called when the user clicks the
     *                widget
     */
    public AbstractButtonWidget(int x, int y, int width, int height, Runnable onPress){
        super(x, y, width, height);
        this.pressable = onPress;
    }

    protected boolean isClickable(){
        return true;
    }

    /**
     * Called when the user clicks the widget.
     */
    public void onPress(){
        playClickSound();
        if(this.pressable != null)
            this.pressable.run();
    }

    @Override
    public CursorType curser(int mouseX, int mouseY){
        return this.isClickable() ? CursorTypes.pointingHand() : null;
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled){
        if(!hasBeenHandled && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height && this.isClickable()){
            this.onPress();
            return true;
        }
        return false;
    }

    /**
     * Plays the default Minecraft button sound.
     */
    public static void playClickSound(){
        ClientUtils.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
