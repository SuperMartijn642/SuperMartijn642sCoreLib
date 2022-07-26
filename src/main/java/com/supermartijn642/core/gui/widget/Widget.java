package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
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
    void renderBackground(PoseStack poseStack, int mouseX, int mouseY);

    /**
     * Renders the widget's main features.
     * Called after the background and slots are drawn, but before items are drawn.
     */
    void render(PoseStack poseStack, int mouseX, int mouseY);

    /**
     * Renders the widget's foreground.
     * Called after main features and items are drawn, but before cursor item and overlay are drawn.
     */
    void renderForeground(PoseStack poseStack, int mouseX, int mouseY);

    /**
     * Called after foreground and cursor item are drawn, but before tooltips are drawn.
     */
    void renderOverlay(PoseStack poseStack, int mouseX, int mouseY);

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    void renderTooltips(PoseStack poseStack, int mouseX, int mouseY);

    /**
     * Called when the widget is disposed of.
     */
    void discard();

    /**
     * Called when a mouse button is pressed down.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param button         the button which is pressed down
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse press
     */
    boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled);

    /**
     * Called when a mouse button is released.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param button         the button which is pressed down
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse release
     */
    boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled);

    /**
     * Called when the mouse wheel is scrolled.
     * @param mouseX         x-position of the mouse
     * @param mouseY         y-position of the mouse
     * @param scrollAmount   the amount the mouse wheel was scrolled by
     * @param hasBeenHandled whether the mouse press has already been handled
     * @return whether this widget has handled the mouse scroll
     */
    boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled);

    /**
     * Called when a key is pressed down.
     * @param keyCode code of the key which was pressed
     * @return whether this widget has handled the key press
     */
    boolean keyPressed(int keyCode, boolean hasBeenHandled);

    /**
     * Called when a key is released.
     * @param keyCode code of the key which was released
     * @return whether this widget has handled the key release
     */
    boolean keyReleased(int keyCode, boolean hasBeenHandled);

    /**
     * Called when a character is typed. May be called in addition to {@link #keyPressed(int, boolean)}.
     * @param character the character which was typed
     * @return whether this widget has handled the character
     */
    boolean charTyped(char character, boolean hasBeenHandled);

}
