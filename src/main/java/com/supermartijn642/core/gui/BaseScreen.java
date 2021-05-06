package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.core.gui.widget.ITickableWidget;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseScreen extends GuiScreen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("supermartijn642corelib", "textures/gui/background.png");

    private final List<Widget> widgets = new LinkedList<>();
    private final List<ITickableWidget> tickableWidgets = new LinkedList<>();
    protected ITextComponent title;
    protected FontRenderer font;

    protected BaseScreen(ITextComponent title){
        this.title = title;
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
    public void setWorldAndResolution(Minecraft mc, int width, int height){
        super.setWorldAndResolution(mc, width, height);
        this.font = this.fontRenderer;
    }

    @Override
    public void initGui(){
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
    public void updateScreen(){
        this.tick();
    }

    public void tick(){
        for(Widget widget : this.widgets)
            if(widget instanceof ITickableWidget)
                ((ITickableWidget)widget).tick();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();

        GlStateManager.translate(this.left(), this.top(), 0);
        mouseX -= this.left();
        mouseY -= this.top();

        GlStateManager.pushMatrix();
        this.render(mouseX, mouseY);
        GlStateManager.popMatrix();
        for(Widget widget : this.widgets){
            widget.blitOffset = this.zLevel;
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
                    this.drawHoveringText(text.getFormattedText(), mouseX, mouseY);
            }
        }
        this.renderTooltips(mouseX, mouseY);
    }

    protected abstract void render(int mouseX, int mouseY);

    protected void renderTooltips(int mouseX, int mouseY){
    }

    protected void drawScreenBackground(float x, float y, float width, float height){
        ScreenUtils.drawScreenBackground(x, y, width, height);
    }

    protected void drawScreenBackground(){
        ScreenUtils.drawScreenBackground(0, 0, this.sizeX(), this.sizeY());
    }

    @Override
    public boolean doesGuiPauseGame(){
        return this.isPauseScreen();
    }

    public boolean isPauseScreen(){
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
        this.mouseClicked((double)mouseX, (double)mouseY, mouseButton);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        mouseX -= this.left();
        mouseY -= this.top();

        this.onMousePress((int)mouseX, (int)mouseY, button);

        for(Widget widget : this.widgets)
            widget.mouseClicked((int)mouseX, (int)mouseY, button);

        try{
            super.mouseClicked((int)mouseX, (int)mouseY, button);
        }catch(IOException ignore){}

        return false;
    }

    protected void onMousePress(int mouseX, int mouseY, int button){
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button){
        this.mouseReleased((double)mouseX, (double)mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button){
        mouseX -= this.left();
        mouseY -= this.top();

        this.onMouseRelease((int)mouseX, (int)mouseY, button);

        for(Widget widget : this.widgets)
            widget.mouseReleased((int)mouseX, (int)mouseY, button);

        super.mouseReleased((int)mouseX, (int)mouseY, button);

        return false;
    }

    protected void onMouseRelease(int mouseX, int mouseY, int button){
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth - (int)this.left();
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - (int)this.top();

        this.mouseScrolled(mouseX, mouseY, Mouse.getEventDWheel() / 120);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        mouseX -= this.left();
        mouseY -= this.top();

        this.onMouseScroll((int)mouseX, (int)mouseY, delta);

        for(Widget widget : this.widgets)
            widget.mouseScrolled((int)mouseX, (int)mouseY, delta);

        return false;
    }

    protected void onMouseScroll(int mouseX, int mouseY, double scroll){
    }

    @Override
    public void handleKeyboardInput() throws IOException{
        if(Keyboard.getEventKeyState()){
            if(Keyboard.getEventCharacter() >= ' ')
                super.handleKeyboardInput();
            if(!this.keyPressed(Keyboard.getEventKey()))
                super.handleKeyboardInput();
        }else{
            if(!this.keyReleased(Keyboard.getEventKey()))
                super.handleKeyboardInput();
        }
    }

    public boolean keyPressed(int keyCode){
        boolean handled = false;

        for(Widget widget : this.widgets){
            if(widget instanceof TextFieldWidget && ((TextFieldWidget)widget).canWrite())
                handled = true;
            widget.keyPressed(keyCode);
        }

        if(handled)
            return true;

        if(keyCode == 1 || ClientUtils.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode)){
            this.closeScreen();
            return true;
        }

        return false;
    }

    public boolean keyReleased(int keyCode){
        for(Widget widget : this.widgets)
            widget.keyReleased(keyCode);

        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode){
        this.charTyped(typedChar);
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
