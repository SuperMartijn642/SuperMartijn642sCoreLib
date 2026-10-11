package com.supermartijn642.core.gui.widget.premade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.CursorType;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.network.chat.Component;

import java.util.Stack;

/**
 * A widget that is a stack of other widgets.
 * <p>
 * Created 11/10/2026 by SuperMartijn642
 */
public final class StackedWidget implements Widget {

    public static StackedWidget create(Widget baseWidget){
        return new StackedWidget(baseWidget);
    }

    private final Widget baseWidget;
    private final Stack<Widget> widgets = new Stack<>();
    private boolean focused = false;

    private StackedWidget(Widget baseWidget){
        this.baseWidget = baseWidget;
        this.widgets.push(baseWidget);
    }

    public void push(Widget widget){
        if(this.focused)
            this.topWidget().setFocused(false);
        this.widgets.push(widget);
        widget.initialize();
        if(this.focused)
            widget.setFocused(true);
    }

    public Widget pop(){
        if(this.widgets.size() <= 1)
            throw new IllegalStateException("No widgets to pop!");
        if(this.focused)
            this.topWidget().setFocused(false);
        Widget popped = this.widgets.pop();
        popped.discard();
        if(this.focused)
            this.topWidget().setFocused(true);
        return popped;
    }

    public Widget baseWidget(){
        return this.baseWidget;
    }

    public Widget topWidget(){
        return this.widgets.peek();
    }

    @Override
    public Component getNarrationMessage(){
        return this.topWidget().getNarrationMessage();
    }

    @Override
    public int width(){
        int left = this.baseWidget.left(), right = this.baseWidget.right();
        for(Widget widget : this.widgets){
            if(widget.left() < left)
                left = widget.left();
            if(widget.right() > right)
                right = widget.right();
        }
        return right - left;
    }

    @Override
    public int height(){
        int top = this.topWidget().top(), bottom = this.topWidget().bottom();
        for(Widget widget : this.widgets){
            if(widget.top() < top)
                top = widget.top();
            if(widget.bottom() > bottom)
                bottom = widget.bottom();
        }
        return bottom - top;
    }

    @Override
    public int left(){
        int left = this.baseWidget.left();
        for(Widget widget : this.widgets){
            if(widget.left() < left)
                left = widget.left();
        }
        return left;
    }

    @Override
    public int top(){
        int top = this.baseWidget.top();
        for(Widget widget : this.widgets){
            if(widget.top() < top)
                top = widget.top();
        }
        return top;
    }

    @Override
    public int right(){
        int right = this.baseWidget.right();
        for(Widget widget : this.widgets){
            if(widget.right() > right)
                right = widget.right();
        }
        return right;
    }

    @Override
    public int bottom(){
        int bottom = this.baseWidget.bottom();
        for(Widget widget : this.widgets){
            if(widget.bottom() > bottom)
                bottom = widget.bottom();
        }
        return bottom;
    }

    @Override
    public void initialize(){
        this.baseWidget.initialize();
    }

    @Override
    public void setFocused(boolean focused){
        this.focused = focused;
        this.topWidget().setFocused(focused);
    }

    @Override
    public void update(){
        for(Widget widget : this.widgets)
            widget.update();
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY){
        for(int i = 0; i < this.widgets.size() - 1; i++){
            Widget widget = this.widgets.get(i);
            widget.renderBackground(poseStack, -100, -100);
            widget.render(poseStack, -100, -100);
            widget.renderForeground(poseStack, -100, -100);
            widget.renderOverlay(poseStack, -100, -100);
            widget.renderTooltips(poseStack, -100, -100);
        }
        ScreenUtils.fillRect(poseStack, -10000, -10000, 20000, 20000, -804253680);
        this.topWidget().renderBackground(poseStack, mouseX, mouseY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY){
        this.topWidget().render(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY){
        this.topWidget().renderForeground(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderOverlay(PoseStack poseStack, int mouseX, int mouseY){
        this.topWidget().renderOverlay(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderTooltips(PoseStack poseStack, int mouseX, int mouseY){
        this.topWidget().renderTooltips(poseStack, mouseX, mouseY);
    }

    @Override
    public void discard(){
        for(Widget widget : this.widgets)
            widget.discard();
    }

    @Override
    public CursorType curser(int mouseX, int mouseY){
        return this.topWidget().curser(mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        return this.topWidget().mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        return this.topWidget().mouseReleased(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        return this.topWidget().mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled);
    }

    @Override
    public boolean keyPressed(int keyCode, boolean hasBeenHandled){
        return this.topWidget().keyPressed(keyCode, hasBeenHandled);
    }

    @Override
    public boolean keyReleased(int keyCode, boolean hasBeenHandled){
        return this.topWidget().keyReleased(keyCode, hasBeenHandled);
    }

    @Override
    public boolean charTyped(char character, boolean hasBeenHandled){
        return this.topWidget().charTyped(character, hasBeenHandled);
    }
}
