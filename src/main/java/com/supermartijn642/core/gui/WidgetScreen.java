package com.supermartijn642.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.MutableWidgetRenderContext;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class WidgetScreen<T extends Widget> extends Screen {

    public static <T extends Widget> WidgetScreen<T> of(T widget){
        return new WidgetScreen<>(widget);
    }

    public static <T extends Widget> WidgetScreen<T> of(T widget, boolean isPauseScreen){
        return new WidgetScreen<>(widget, isPauseScreen);
    }

    private final MutableWidgetRenderContext widgetRenderContext = MutableWidgetRenderContext.create();
    protected final T widget;
    private boolean initialized = false;
    private boolean isPauseScreen = false;

    public WidgetScreen(T widget, boolean isPauseScreen){
        super(TextComponents.empty().get());
        this.widget = widget;
        this.isPauseScreen = isPauseScreen;
    }

    public WidgetScreen(T widget){
        this(widget, false);
    }

    @Override
    protected void init(){
        if(!this.initialized){
            this.widget.initialize();
            this.initialized = true;
        }
    }

    @Override
    public void onClose(){
        this.widget.discard();
        super.onClose();
    }

    @Override
    public void tick(){
        this.widget.update();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        this.widgetRenderContext.update(guiGraphics, partialTicks);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;

        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().translate(offsetX, offsetY, 0);

        // Update whether the widget is focused
        this.widget.setFocused(mouseX >= 0 && mouseX < this.widget.width() && mouseY >= 0 && mouseY < this.widget.height());

        // Render the widget background
        this.widget.renderBackground(this.widgetRenderContext, mouseX, mouseY);
        // Render the widget
        this.widget.render(this.widgetRenderContext, mouseX, mouseY);
        // Render the widget's foreground
        this.widget.renderForeground(this.widgetRenderContext, mouseX, mouseY);
        // Render the widget's overlay
        this.widget.renderOverlay(this.widgetRenderContext, mouseX, mouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(this.widgetRenderContext, mouseX, mouseY);

        RenderSystem.getModelViewStack().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        return this.widget.mousePressed((int)mouseX, (int)mouseY, button, false) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        return this.widget.mouseReleased((int)mouseX, (int)mouseY, button, false) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        return this.widget.mouseScrolled((int)mouseX, (int)mouseY, horizontalAmount, false) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(this.widget.keyPressed(keyCode, false))
            return true;

        if(ClientUtils.getMinecraft().options.keyInventory.matches(keyCode, scanCode)){
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers){
        return this.widget.keyReleased(keyCode, false) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers){
        return this.widget.charTyped(character, false) || super.charTyped(character, modifiers);
    }

    @Override
    public boolean isPauseScreen(){
        return this.isPauseScreen;
    }

    @Override
    public Component getNarrationMessage(){
        Component message = this.widget.getNarrationMessage();
        return message == null ? TextComponents.empty().get() : message;
    }
}
