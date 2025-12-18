package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.MutableWidgetRenderContext;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    private boolean dragging = false;

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
        this.widgetRenderContext.update(guiGraphics, partialTicks, this.font, this.minecraft);

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(offsetX, offsetY);

        // Update whether the widget is focused
        if(!this.dragging)
            this.widget.setFocused(mouseX >= 0 && mouseX < this.widget.width() && mouseY >= 0 && mouseY < this.widget.height());

        GuiGraphicsHelper helper = GuiGraphicsHelper.of(guiGraphics);
        // Render the widget background
        this.widget.renderBackground(this.widgetRenderContext, helper, mouseX, mouseY);
        // Render the widget
        this.widget.render(this.widgetRenderContext, helper, mouseX, mouseY);
        // Render the widget's foreground
        this.widget.renderForeground(this.widgetRenderContext, helper, mouseX, mouseY);
        // Render the widget's overlay
        this.widget.renderOverlay(this.widgetRenderContext, helper, mouseX, mouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(this.widgetRenderContext, helper, mouseX, mouseY);

        guiGraphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick){
        this.dragging = true;
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int mouseX = (int)event.x() - offsetX;
        int mouseY = (int)event.y() - offsetY;
        return this.widget.mousePressed(mouseX, mouseY, event.buttonInfo(), isDoubleClick, false) || super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event){
        this.dragging = false;
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int mouseX = (int)event.x() - offsetX;
        int mouseY = (int)event.y() - offsetY;
        return this.widget.mouseReleased(mouseX, mouseY, event.buttonInfo(), false) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int mouseX = (int)event.x() - offsetX;
        int mouseY = (int)event.y() - offsetY;
        return this.widget.mouseDragged(mouseX, mouseY, event.buttonInfo(), deltaX, deltaY, false) || super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        return this.widget.mouseScrolled((int)mouseX, (int)mouseY, verticalAmount, false) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event){
        if(this.widget.keyPressed(event, false))
            return true;

        if(ClientUtils.getMinecraft().options.keyInventory.matches(event)){
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event){
        return this.widget.keyReleased(event, false) || super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event){
        return this.widget.charTyped((char)event.codepoint(), false) || super.charTyped(event);
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
