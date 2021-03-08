package com.supermartijn642.core.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.core.gui.widget.ITickableWidget;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public abstract class BaseContainerScreen<T extends BaseContainer> extends ContainerScreen<T> {

    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("supermartijn642corelib", "textures/gui/slot.png");

    protected final List<Widget> widgets = new LinkedList<>();
    protected final List<ITickableWidget> tickableWidgets = new LinkedList<>();

    protected final T container;
    private boolean drawSlots = true;

    public BaseContainerScreen(T screenContainer, ITextComponent title){
        super(screenContainer, screenContainer.player.inventory, title);
        this.container = screenContainer;
    }

    protected abstract int sizeX();

    protected abstract int sizeY();

    protected int left(){
        return (this.width - this.sizeX()) / 2;
    }

    protected int top(){
        return (this.height - this.sizeY()) / 2;
    }

    @Override
    public int getXSize(){
        return this.sizeX();
    }

    @Override
    public int getYSize(){
        return this.sizeY();
    }

    @Override
    public int getGuiLeft(){
        return this.left();
    }

    @Override
    public int getGuiTop(){
        return this.top();
    }

    protected void setDrawSlots(boolean drawSlots){
        this.drawSlots = drawSlots;
    }

    @Override
    public void init(){
        this.xSize = this.sizeX();
        this.ySize = this.sizeY();
        super.init();

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
        this.renderBackground(mouseX - this.left(), mouseY - this.top());

        if(this.drawSlots){
            for(Slot slot : this.container.inventorySlots){
                Minecraft.getInstance().getTextureManager().bindTexture(SLOT_TEXTURE);
                ScreenUtils.drawTexture(slot.xPos - 1, slot.yPos - 1, 18, 18);
            }
        }
        GlStateManager.translated(-this.left(), -this.top(), 0);

        super.render(mouseX, mouseY, partialTicks);
        // apparently some OpenGl settings are messed up after this

        GlStateManager.enableAlphaTest();
        GlStateManager.disableLighting();

        GlStateManager.translated(this.left(), this.top(), 0);
        for(Widget widget : this.widgets){
            widget.blitOffset = this.blitOffset;
            widget.wasHovered = widget.hovered;
            widget.hovered = mouseX - this.left() > widget.x && mouseX - this.left() < widget.x + widget.width &&
                mouseY - this.top() > widget.y && mouseY - this.top() < widget.y + widget.height;
            widget.render(mouseX - this.left(), mouseY - this.top(), partialTicks);
            widget.narrate();
        }

        this.renderForeground(mouseX - this.left(), mouseY - this.top());

        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(text.getFormattedText(), mouseX - this.left(), mouseY - this.top());
            }
        }
        GlStateManager.translated(-this.left(), -this.top(), 0);
        super.renderHoveredToolTip(mouseX, mouseY);
        this.renderTooltips(mouseX - this.left(), mouseY - this.top());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
    }

    protected void renderBackground(int mouseX, int mouseY){
        this.drawScreenBackground();
    }

    protected void renderForeground(int mouseX, int mouseY){
        ScreenUtils.drawString(this.font, this.title, 8, 7, 4210752);
    }

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

        mouseX += this.left();
        mouseY += this.top();

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

        mouseX += this.left();
        mouseY += this.top();

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

        mouseX += this.left();
        mouseY += this.top();

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    protected void onMouseScroll(int mouseX, int mouseY, double scroll){
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(this.keyReleased(keyCode))
            return true;

        InputMappings.Input key = InputMappings.getInputByCode(keyCode, scanCode);
        if(ClientUtils.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(key))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean keyPressed(int keyCode){
        boolean handled = false;

        for(Widget widget : this.widgets){
            widget.keyPressed(keyCode);
            if(widget instanceof TextFieldWidget && ((TextFieldWidget)widget).isFocused())
                handled = true;
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
}
