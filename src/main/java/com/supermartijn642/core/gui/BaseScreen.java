package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.core.gui.widget.ITickableWidget;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseScreen extends Screen {

    private final List<Widget> widgets = new LinkedList<>();
    private final List<ITickableWidget> tickableWidgets = new LinkedList<>();

    /**
     * @param title title to be read by the narrator and to be displayed in the gui
     */
    protected BaseScreen(ITextComponent title){
        super(title);
    }

    /**
     * @return the width of the screen
     */
    protected abstract float sizeX();

    /**
     * @return the height of the screen
     */
    protected abstract float sizeY();

    /**
     * @return the left edge of the screen
     */
    protected float left(){
        return (this.width - this.sizeX()) / 2;
    }

    /**
     * @return the top edge of the screen
     */
    protected float top(){
        return (this.height - this.sizeY()) / 2;
    }

    @Override
    protected void init(){
        this.widgets.clear();
        this.tickableWidgets.clear();
        this.addWidgets();
    }

    /**
     * Adds widgets to the screen via {@link #addWidget(Widget)}.
     */
    protected abstract void addWidgets();

    /**
     * Add the given {@code widget} to the screen.
     * @param widget widget to be added
     * @return the given {@code widget}
     */
    protected <T extends Widget> T addWidget(T widget){
        this.widgets.add(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.add((ITickableWidget)widget);
        return widget;
    }

    /**
     * Removes the given {@code widget} from the screen.
     * @param widget widget to be removed
     * @return the given {@code widget}
     */
    protected <T extends Widget> T removeWidget(T widget){
        this.widgets.remove(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.remove(widget);
        return widget;
    }

    @Override
    public void tick(){
        this.tickableWidgets.forEach(ITickableWidget::tick);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);

        matrixStack.push();
        matrixStack.translate(this.left(), this.top(), 0);
        mouseX -= this.left();
        mouseY -= this.top();

        this.renderBackground(matrixStack, mouseX, mouseY);

        this.render(matrixStack, mouseX, mouseY);

        for(Widget widget : this.widgets){
            widget.blitOffset = this.getBlitOffset();
            widget.wasHovered = widget.hovered;
            widget.hovered = mouseX > widget.x && mouseX < widget.x + widget.width &&
                mouseY > widget.y && mouseY < widget.y + widget.height;
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
            widget.narrate();
        }

        this.renderForeground(matrixStack, mouseX, mouseY);

        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(matrixStack, text, mouseX, mouseY);
            }
        }
        this.renderTooltips(matrixStack, mouseX, mouseY);

        matrixStack.pop();
    }

    /**
     * Renders the screen's background. This will be called first in the render chain.
     */
    protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY){
    }

    /**
     * Renders the screen's main features.
     * Called after the background and slots are drawn, but before widgets, items, and tooltips are drawn.
     */
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
    }

    /**
     * Renders the screen's foreground.
     * Called after widgets are drawn, but before tooltips are drawn.
     */
    protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY){
    }

    /**
     * Renders tooltips for the given {@code mouseX} and {@code mouseY}.
     * This will be called last in the render chain.
     */
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){
    }

    /**
     * Draws the default screen background.
     * Same as {@link ScreenUtils#drawScreenBackground(MatrixStack, float, float, float, float)}.
     */
    protected void drawScreenBackground(MatrixStack matrixStack, float x, float y, float width, float height){
        ScreenUtils.drawScreenBackground(matrixStack, x, y, width, height);
    }

    /**
     * Draws the default screen background with width {@link #sizeX()} and height {@link #sizeY()}.
     */
    protected void drawScreenBackground(MatrixStack matrixStack){
        ScreenUtils.drawScreenBackground(matrixStack, 0, 0, this.sizeX(), this.sizeY());
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        mouseX -= this.left();
        mouseY -= this.top();

        this.onMousePress((int)mouseX, (int)mouseY, button);

        for(Widget widget : this.widgets)
            widget.mouseClicked((int)mouseX, (int)mouseY, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Called whenever a mouse button is pressed down.
     */
    protected void onMousePress(int mouseX, int mouseY, int button){
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        mouseX -= this.left();
        mouseY -= this.top();

        this.onMouseRelease((int)mouseX, (int)mouseY, button);

        for(Widget widget : this.widgets)
            widget.mouseReleased((int)mouseX, (int)mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Called whenever a mouse button is released.
     */
    protected void onMouseRelease(int mouseX, int mouseY, int button){
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        mouseX -= this.left();
        mouseY -= this.top();

        this.onMouseScroll((int)mouseX, (int)mouseY, delta);

        for(Widget widget : this.widgets)
            widget.mouseScrolled((int)mouseX, (int)mouseY, delta);

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    /**
     * Called whenever the user performs a scroll action.
     */
    protected void onMouseScroll(int mouseX, int mouseY, double scroll){
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(this.keyPressed(keyCode))
            return true;

        InputMappings.Input key = InputMappings.getInputByCode(keyCode, scanCode);
        if(ClientUtils.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(key)){
            this.closeScreen();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Called whenever a key is pressed down.
     */
    public boolean keyPressed(int keyCode){
        boolean handled = false;

        for(Widget widget : this.widgets){
            if(widget instanceof TextFieldWidget && ((TextFieldWidget)widget).canWrite())
                handled = true;
            widget.keyPressed(keyCode);
        }

        return handled;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers){
        return this.keyReleased(keyCode);
    }

    /**
     * Called whenever a key is released.
     */
    public boolean keyReleased(int keyCode){
        for(Widget widget : this.widgets)
            widget.keyReleased(keyCode);

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers){
        return this.charTyped(codePoint);
    }

    /**
     * Called whenever a character key is released with the given character {@code c}.
     */
    public boolean charTyped(char c){
        for(Widget widget : this.widgets)
            widget.charTyped(c);

        return false;
    }
}
