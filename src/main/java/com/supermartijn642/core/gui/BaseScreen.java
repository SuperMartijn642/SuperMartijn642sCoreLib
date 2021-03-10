package com.supermartijn642.core.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.core.gui.widget.ITickableWidget;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseScreen extends Screen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("supermartijn642corelib", "textures/gui/background.png");

    private final List<Widget> widgets = new LinkedList<>();
    private final List<ITickableWidget> tickableWidgets = new LinkedList<>();

    protected BaseScreen(ITextComponent title){
        super(title);
    }

    protected abstract float sizeX();

    protected abstract float sizeY();

    protected float left(){
        return (this.width - this.sizeX()) / 2;
    }

    protected float top(){
        return (this.height - this.sizeY()) / 2;
    }

    @Override
    protected void init(){
        this.widgets.clear();
        this.tickableWidgets.clear();
        this.addWidgets();
    }

    protected abstract void addWidgets();

    protected <T extends Widget> T addWidget(T widget){
        this.widgets.add(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.add((ITickableWidget)widget);
        return widget;
    }

    protected <T extends Widget> T removeWidget(T widget){
        this.widgets.remove(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.remove(widget);
        return widget;
    }

    @Override
    public void tick(){
        for(Widget widget : this.widgets)
            if(widget instanceof ITickableWidget)
                ((ITickableWidget)widget).tick();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        this.renderBackground();

        GlStateManager.translated(this.left(), this.top(), 0);
        mouseX -= this.left();
        mouseY -= this.top();

        GlStateManager.pushMatrix();
        this.render(mouseX, mouseY);
        GlStateManager.popMatrix();
        for(Widget widget : this.widgets){
            widget.blitOffset = this.getBlitOffset();
            widget.wasHovered = widget.hovered;
            widget.hovered = mouseX > widget.x && mouseX < widget.x + widget.width &&
                mouseY > widget.y && mouseY < widget.y + widget.height;
            widget.render(mouseX, mouseY, partialTicks);
            widget.narrate();
        }
        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(text.getFormattedText(), mouseX, mouseY);
            }
        }
        this.renderTooltips(mouseX, mouseY);
    }

    protected abstract void render(int mouseX, int mouseY);

    protected abstract void renderTooltips(int mouseX, int mouseY);

    protected void drawScreenBackground(float x, float y, float width, float height){
        ScreenUtils.drawScreenBackground(x, y, width, height);
    }

    protected void drawScreenBackground(){
        ScreenUtils.drawScreenBackground(0, 0, this.sizeX(), this.sizeY());
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

    protected void onMouseScroll(int mouseX, int mouseY, double scroll){
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(this.keyPressed(keyCode))
            return true;

        InputMappings.Input key = InputMappings.getInputByCode(keyCode, scanCode);
        if(ClientUtils.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(key))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

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

    public boolean keyReleased(int keyCode){
        for(Widget widget : this.widgets)
            widget.keyReleased(keyCode);

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers){
        return this.charTyped(codePoint);
    }

    public boolean charTyped(char c){
        for(Widget widget : this.widgets)
            widget.charTyped(c);

        return false;
    }

    protected void closeScreen(){
        ClientUtils.closeScreen();
    }
}
