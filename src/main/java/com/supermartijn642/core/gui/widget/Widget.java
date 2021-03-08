package com.supermartijn642.core.gui.widget;

import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class Widget {

    public int x, y;
    public int width, height;
    public boolean active = true;
    public boolean hovered = false, wasHovered = false;
    protected long nextNarration = Long.MAX_VALUE;
    public float blitOffset = 0;

    public Widget(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void narrate(){
        if(this.wasHovered != this.hovered)
            this.nextNarration = this.hovered ? System.currentTimeMillis() + (long)750 : Long.MAX_VALUE;

        if(this.active && this.hovered && System.currentTimeMillis() > this.nextNarration){
            ITextComponent message = this.getNarrationMessage();
            if(message != null){
                NarratorChatListener.INSTANCE.say(ChatType.GAME_INFO, message);
                this.nextNarration = Long.MAX_VALUE;
            }
        }
    }

    protected abstract ITextComponent getNarrationMessage();

    public void setActive(boolean active){
        this.active = active;
    }

    public abstract void render(int mouseX, int mouseY, float partialTicks);

    public boolean isHovered(){
        return this.hovered;
    }

    public void mouseClicked(int mouseX, int mouseY, int button){
    }

    public void mouseReleased(int mouseX, int mouseY, int button){
    }

    public void mouseScrolled(int mouseX, int mouseY, double scroll){
    }

    public void keyPressed(int keyCode){
    }

    public void keyReleased(int keyCode){
    }

    public void charTyped(char c){
    }
}
