package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.core.gui.widget.ITickableWidget;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);

        matrixStack.translate(this.left(), this.top(), 0);
        this.renderBackground(matrixStack, mouseX - this.left(), mouseY - this.top());

        if(this.drawSlots){
            for(Slot slot : this.container.inventorySlots){
                Minecraft.getInstance().getTextureManager().bindTexture(SLOT_TEXTURE);
                ScreenUtils.drawTexture(matrixStack, slot.xPos - 1, slot.yPos - 1, 18, 18);
            }
        }
        matrixStack.translate(-this.left(), -this.top(), 0);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        // apparently some OpenGl settings are messed up after this

        RenderSystem.enableAlphaTest();
        GlStateManager.disableLighting();

        matrixStack.translate(this.left(), this.top(), 0);
        for(Widget widget : this.widgets){
            widget.blitOffset = this.getBlitOffset();
            widget.wasHovered = widget.hovered;
            widget.hovered = mouseX - this.left() > widget.x && mouseX - this.left() < widget.x + widget.width &&
                mouseY - this.top() > widget.y && mouseY - this.top() < widget.y + widget.height;
            widget.render(matrixStack, mouseX - this.left(), mouseY - this.top(), partialTicks);
            widget.narrate();
        }

        this.renderForeground(matrixStack, mouseX - this.left(), mouseY - this.top());

        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(matrixStack, text, mouseX - this.left(), mouseY - this.top());
            }
        }
        matrixStack.translate(-this.left(), -this.top(), 0);
        super.renderHoveredTooltip(matrixStack, mouseX, mouseY);
        this.renderTooltips(matrixStack, mouseX - this.left(), mouseY - this.top());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y){
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y){
    }

    protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY){
        this.drawScreenBackground(matrixStack);
    }

    protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY){
        ScreenUtils.drawString(matrixStack, this.font, this.title, 8, 7, 4210752);
    }

    protected abstract void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY);

    protected void drawScreenBackground(MatrixStack matrixStack, float x, float y, float width, float height){
        ScreenUtils.drawScreenBackground(matrixStack, x, y, width, height);
    }

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

        mouseX += this.left();
        mouseY += this.top();

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void onMousePress(int mouseX, int mouseY, int button){
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
        mouseX -= this.left();
        mouseY -= this.top();

        for(Widget widget : this.widgets)
            widget.mouseDragged((int)mouseX, (int)mouseY, button);

        mouseX += this.left();
        mouseY += this.top();

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
        boolean handled = false;

        for(Widget widget : this.widgets){
            widget.keyPressed(keyCode, scanCode, modifiers);
            if(widget instanceof TextFieldWidget && ((TextFieldWidget)widget).isFocused())
                handled = true;
        }

        if(handled)
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers){
        for(Widget widget : this.widgets)
            widget.keyReleased(keyCode, scanCode, modifiers);

        return false;
    }
}
