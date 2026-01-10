package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public interface Widget {

    /**
     * @return the title to be read by the narrator when the widget is focused by the user
     */
    Component getNarrationMessage();

    /**
     * @return the width of the widget
     */
    int width();

    /**
     * @return the height of the widget
     */
    int height();

    /**
     * @return the x-position of this widget
     */
    int left();

    /**
     * @return the y-position of this widget
     */
    int top();

    /**
     * Called when the widget is added.
     */
    void initialize();

    /**
     * Sets whether this widget is the one the user is focused on.
     */
    void setFocused(boolean focused);

    /**
     * Called once per tick when the widget is shown.
     */
    void update();

    /**
     * Renders the widget's background. This will be called first in the render chain.
     */
    void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY);

    /**
     * Renders the widget's main features.
     * Called after the background and slots are drawn, but before items are drawn.
     */
    void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY);

    /**
     * Renders the widget's foreground.
     * Called after main features and items are drawn, but before cursor item and overlay are drawn.
     */
    void renderForeground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY);

    /**
     * Called after foreground and cursor item are drawn, but before tooltips are drawn.
     */
    void renderOverlay(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY);

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY);

    /**
     * Called when the widget is disposed of.
     */
    void discard();

    /**
     * Gets the cursor to be used when hovering this widget.
     */
    CursorType curser(int mouseX, int mouseY);

    /**
     * Called when a mouse button is pressed down.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param info           the button which is pressed down
     * @param isDoubleClick  whether the even is a double click
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse press
     */
    boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled);

    /**
     * Called when a mouse button is released.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param info           the button which is released
     * @param hasBeenHandled whether the mouse release has already been handled
     * @return whether this widget has handled the mouse release
     */
    boolean mouseReleased(int mouseX, int mouseY, MouseButtonInfo info, boolean hasBeenHandled);

    /**
     * Called whilst a mouse button is pressed and the mouse position is moved.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param info           the button which is pressed down
     * @param deltaX         horizontal movement since the mouse button was pressed
     * @param deltaY         vertical movement since the mouse button was pressed
     * @param hasBeenHandled whether the mouse drag has already been handled
     * @return whether this widget has handled the mouse drag
     */
    boolean mouseDragged(int mouseX, int mouseY, MouseButtonInfo info, double deltaX, double deltaY, boolean hasBeenHandled);

    /**
     * Called when the mouse wheel is scrolled.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param scrollAmount   the amount the mouse wheel was scrolled by
     * @param hasBeenHandled whether the mouse scroll has already been handled
     * @return whether this widget has handled the mouse scroll
     */
    boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled);

    /**
     * Called when a key is pressed down.
     * @param event data on the key which was pressed
     * @return whether this widget has handled the key press
     */
    boolean keyPressed(KeyEvent event, boolean hasBeenHandled);

    /**
     * Called when a key is released.
     * @param event data on the key which was pressed
     * @return whether this widget has handled the key release
     */
    boolean keyReleased(KeyEvent event, boolean hasBeenHandled);

    /**
     * Called when a character is typed. May be called in addition to {@link #keyReleased(KeyEvent, boolean)}.
     * @param character the character which was typed
     * @return whether this widget has handled the character
     */
    boolean charTyped(char character, boolean hasBeenHandled);

}
