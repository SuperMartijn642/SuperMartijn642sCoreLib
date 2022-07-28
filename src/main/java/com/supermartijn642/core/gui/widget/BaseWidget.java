package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public abstract class BaseWidget implements Widget {

    protected final List<Widget> widgets = new ArrayList<>();
    protected Widget focusedWidget = null;
    protected int x, y, width, height;
    private boolean focused;

    public BaseWidget(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int width(){
        return this.width;
    }

    @Override
    public int height(){
        return this.height;
    }

    @Override
    public int left(){
        return this.x;
    }

    @Override
    public int top(){
        return this.y;
    }

    @Override
    public void initialize(){
        this.addWidgets();
        this.widgets.forEach(Widget::initialize);
    }

    @Override
    public void setFocused(boolean focused){
        this.focused = focused;
    }

    public boolean isFocused(){
        return this.focused;
    }

    /**
     * Adds widgets to the screen via {@link #addWidget(Widget)}.
     */
    protected void addWidgets(){
    }

    /**
     * Add the given {@code widget} to the screen.
     * @param widget widget to be added
     * @return the given {@code widget}
     */
    protected <T extends Widget> T addWidget(T widget){
        if(widget == null)
            throw new IllegalArgumentException("Widget must not be null!");
        if(widget == this)
            throw new IllegalArgumentException("Cannot add a widget to itself!");
        if(widget instanceof ContainerWidget<?>)
            throw new IllegalArgumentException("Cannot add a container widget to a regular widget!");
        this.widgets.add(widget);
        return widget;
    }

    /**
     * Removes the given {@code widget} from the screen.
     * @param widget widget to be removed
     * @return true if this widget contained the given widget
     */
    protected boolean removeWidget(Widget widget){
        return this.widgets.remove(widget);
    }

    @Override
    public void update(){
        this.widgets.forEach(Widget::update);
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY){
        // Update the focused widget
        if(!this.focused)
            this.focusedWidget = null;
        else if(this.focusedWidget != null && !(mouseX > this.focusedWidget.left() && mouseX < this.focusedWidget.left() + this.focusedWidget.width() && mouseY > this.focusedWidget.top() && mouseY < this.focusedWidget.top() + this.focusedWidget.height()))
            this.focusedWidget = null;
        for(Widget widget : this.widgets){
            if(this.focusedWidget == null && mouseX >= widget.left() && mouseX < widget.left() + widget.width() && mouseY >= widget.top() && mouseY < widget.top() + widget.height()){
                this.focusedWidget = widget;
                widget.setFocused(true);
            }else
                widget.setFocused(widget == this.focusedWidget);
        }

        // Render internal widgets' background
        this.widgets.stream().filter(w -> w != this.focusedWidget).forEach(w -> w.renderBackground(poseStack, mouseX, mouseY));
        if(this.focusedWidget != null)
            this.focusedWidget.renderBackground(poseStack, mouseX, mouseY);
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY){
        // Render internal widgets
        this.widgets.stream().filter(w -> w != this.focusedWidget).forEach(w -> w.render(poseStack, mouseX, mouseY));
        if(this.focusedWidget != null)
            this.focusedWidget.render(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY){
        // Render internal widgets' foreground
        this.widgets.stream().filter(w -> w != this.focusedWidget).forEach(w -> w.renderForeground(poseStack, mouseX, mouseY));
        if(this.focusedWidget != null)
            this.focusedWidget.renderForeground(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderOverlay(MatrixStack poseStack, int mouseX, int mouseY){
        // Render internal widgets
        this.widgets.stream().filter(w -> w != this.focusedWidget).forEach(w -> w.renderOverlay(poseStack, mouseX, mouseY));
        if(this.focusedWidget != null)
            this.focusedWidget.renderOverlay(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderTooltips(MatrixStack poseStack, int mouseX, int mouseY){
        if(this.focused){
            if(this.focusedWidget != null)
                this.focusedWidget.renderTooltips(poseStack, mouseX, mouseY);
            else{
                // Find a better way to do this, preferably without instantiating an array list unless needed
                List<ITextComponent> tooltips = new ArrayList<>(0);
                this.getTooltips(tooltips::add);
                ScreenUtils.drawTooltip(poseStack, tooltips, mouseX, mouseY);
            }
        }
    }

    /**
     * Gathers the tooltips to be rendered in {@link #renderTooltips(MatrixStack, int, int)}. Tooltips will only be shown when this widget is focused.
     * @param tooltips consumer for tooltips to be rendered
     */
    protected void getTooltips(Consumer<ITextComponent> tooltips){
    }

    @Override
    public void discard(){
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(this.focusedWidget != null)
            hasBeenHandled = this.focusedWidget.mousePressed(mouseX, mouseY, button, hasBeenHandled) || hasBeenHandled;
        for(Widget widget : this.widgets){
            if(widget != this.focusedWidget)
                hasBeenHandled = widget.mousePressed(mouseX, mouseY, button, hasBeenHandled) || hasBeenHandled;
        }
        return hasBeenHandled;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(this.focusedWidget != null)
            hasBeenHandled = this.focusedWidget.mouseReleased(mouseX, mouseY, button, hasBeenHandled) || hasBeenHandled;
        for(Widget widget : this.widgets){
            if(widget != this.focusedWidget)
                hasBeenHandled = widget.mouseReleased(mouseX, mouseY, button, hasBeenHandled) || hasBeenHandled;
        }
        return hasBeenHandled;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        if(this.focusedWidget != null)
            hasBeenHandled = this.focusedWidget.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled) || hasBeenHandled;
        for(Widget widget : this.widgets){
            if(widget != this.focusedWidget)
                hasBeenHandled = widget.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled) || hasBeenHandled;
        }
        return hasBeenHandled;
    }

    @Override
    public boolean keyPressed(int keyCode, boolean hasBeenHandled){
        if(this.focusedWidget != null)
            hasBeenHandled = this.focusedWidget.keyPressed(keyCode, hasBeenHandled) || hasBeenHandled;
        for(Widget widget : this.widgets){
            if(widget != this.focusedWidget)
                hasBeenHandled = widget.keyPressed(keyCode, hasBeenHandled) || hasBeenHandled;
        }
        return hasBeenHandled;
    }

    @Override
    public boolean keyReleased(int keyCode, boolean hasBeenHandled){
        if(this.focusedWidget != null)
            hasBeenHandled = this.focusedWidget.keyReleased(keyCode, hasBeenHandled) || hasBeenHandled;
        for(Widget widget : this.widgets){
            if(widget != this.focusedWidget)
                hasBeenHandled = widget.keyReleased(keyCode, hasBeenHandled) || hasBeenHandled;
        }
        return hasBeenHandled;
    }

    @Override
    public boolean charTyped(char character, boolean hasBeenHandled){
        if(this.focusedWidget != null)
            hasBeenHandled = this.focusedWidget.charTyped(character, hasBeenHandled) || hasBeenHandled;
        for(Widget widget : this.widgets){
            if(widget != this.focusedWidget)
                hasBeenHandled = widget.charTyped(character, hasBeenHandled) || hasBeenHandled;
        }
        return hasBeenHandled;
    }
}
