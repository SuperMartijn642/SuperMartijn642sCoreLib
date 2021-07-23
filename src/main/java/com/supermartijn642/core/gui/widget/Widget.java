package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;

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
            this.nextNarration = this.hovered ? Util.getMillis() + (long)750 : Long.MAX_VALUE;

        if(this.active && this.hovered && Util.getMillis() > this.nextNarration){
            Component message = this.getNarrationMessage();
            String s = message == null ? "" : message.getString();
            if(!s.isEmpty()){
                NarratorChatListener.INSTANCE.sayNow(s);
                this.nextNarration = Long.MAX_VALUE;
            }
        }
    }

    /**
     * @return the message that should be narrated for the current state of the widget
     */
    protected abstract Component getNarrationMessage();

    /**
     * Sets whether the widget is active, i.e. can be interacted with.
     * The way the widget is rendered can also change base on whether the
     * widget is active.
     */
    public void setActive(boolean active){
        this.active = active;
    }

    /**
     * Renders the entire widget.
     */
    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks);

    /**
     * @return whether the user is hovering their cursor over the widget
     */
    public boolean isHovered(){
        return this.hovered;
    }

    /**
     * Called whenever a mouse button is pressed down.
     */
    public void mouseClicked(int mouseX, int mouseY, int button){
    }

    /**
     * Called whenever a mouse button is released.
     */
    public void mouseReleased(int mouseX, int mouseY, int button){
    }

    /**
     * Called whenever the user performs a scroll action.
     */
    public void mouseScrolled(int mouseX, int mouseY, double scroll){
    }

    /**
     * Called whenever a key is pressed down.
     */
    public void keyPressed(int keyCode){
    }

    /**
     * Called whenever a key is released.
     */
    public void keyReleased(int keyCode){
    }

    /**
     * Called whenever a character key is released with the given character {@code c}.
     */
    public void charTyped(char c){
    }
}
