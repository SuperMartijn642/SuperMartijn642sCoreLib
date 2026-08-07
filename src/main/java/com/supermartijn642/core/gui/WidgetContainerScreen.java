package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.ContainerWidget;
import com.supermartijn642.core.gui.widget.MutableWidgetRenderContext;
import com.supermartijn642.core.gui.widget.Widget;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class WidgetContainerScreen<T extends Widget, X extends BaseContainer> extends AbstractContainerScreen<X> {

    public static <T extends Widget, X extends BaseContainer> WidgetContainerScreen<T,X> of(T widget, X container, boolean drawSlots, boolean isPauseScreen){
        return new WidgetContainerScreen<>(widget, container, drawSlots, isPauseScreen);
    }

    public static <T extends Widget, X extends BaseContainer> WidgetContainerScreen<T,X> of(T widget, X container, boolean drawSlots){
        return new WidgetContainerScreen<>(widget, container, drawSlots);
    }

    private final MutableWidgetRenderContext widgetRenderContext = MutableWidgetRenderContext.create();
    protected final X container;
    protected final T widget;
    private boolean initialized = false;
    private final boolean drawSlots;
    private final boolean isPauseScreen;

    public WidgetContainerScreen(T widget, X container, boolean drawSlots, boolean isPauseScreen){
        super(container, container.player.getInventory(), TextComponents.empty().get());
        this.widget = widget;
        this.container = container;
        this.drawSlots = drawSlots;
        this.isPauseScreen = isPauseScreen;
    }

    public WidgetContainerScreen(T widget, X container, boolean drawSlots){
        this(widget, container, drawSlots, false);
    }

    public T getWidget(){
        return this.widget;
    }

    @Override
    public void init(){
        if(!this.initialized){
            if(this.widget instanceof ContainerWidget<?>)
                //noinspection unchecked,rawtypes
                ((ContainerWidget)this.widget).initialize(this.container);
            else
                this.widget.initialize();
            this.initialized = true;
        }

        this.imageWidth = this.widget.width();
        this.imageHeight = this.widget.height();
        super.init();
    }

    @Override
    public void onClose(){
        this.widget.discard();
        super.onClose();
    }

    @Override
    protected void containerTick(){
        this.widget.update();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        this.widgetRenderContext.update(guiGraphics, partialTicks, this.font, this.minecraft);
        GuiGraphicsHelper helper = GuiGraphicsHelper.of(guiGraphics);

        // Call Architectury's client events
        if(CoreLib.isArchitecturyLoaded)
            ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.invoker().render(this, guiGraphics, mouseX, mouseY, partialTicks);

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int offsetMouseX = mouseX - offsetX;
        int offsetMouseY = mouseY - offsetY;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(offsetX, offsetY);

        // Update whether the widget is focused
        this.widget.setFocused(offsetMouseX >= 0 && offsetMouseX < this.widget.width() && offsetMouseY >= 0 && offsetMouseY < this.widget.height());

        // Update cursor
        CursorType curser = this.widget.curser(offsetMouseX, offsetMouseY);
        if(curser != null)
            helper.requestCursor(curser);

        // Render the widget background
        this.widget.renderBackground(this.widgetRenderContext, helper, offsetMouseX, offsetMouseY);

        if(this.drawSlots){
            for(Slot slot : this.container.slots){
                if(!slot.isActive())
                    continue;
                if(slot instanceof CustomSlot customSlot){
                    if(customSlot.showBackground())
                        helper.submitDefaultSlot(slot.x - 1, slot.y - 1, customSlot.getWidth(), customSlot.getHeight());
                }else
                    helper.submitDefaultSlot(slot.x - 1, slot.y - 1);
            }
        }

        // Render the widget
        this.widget.render(this.widgetRenderContext, helper, offsetMouseX, offsetMouseY);

        this.hoveredSlot = null;
        for(Slot slot : this.container.slots){
            if(!slot.isActive())
                continue;

            if(slot instanceof CustomSlot customSlot){
                // Custom slot
                int slotWidth = customSlot.getWidth();
                int slotHeight = customSlot.getHeight();
                if(this.isHovering(slot.x, slot.y, slotWidth - 2, slotHeight - 2, mouseX, mouseY)){
                    this.hoveredSlot = slot;
                    if(customSlot.showHighlight() && slot.isHighlightable())
                        helper.submitSprite(AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE, slot.x - 4, slot.y - 4, slotWidth + 6, slotHeight + 6);
                }
                if(customSlot.showItem()){
                    float scale = Math.min(slotWidth / 18f, slotHeight / 18f);
                    guiGraphics.pose().pushMatrix();
                    if(customSlot.scaleItemToSize() && scale != 1){
                        guiGraphics.pose().translate(slot.x, slot.y);
                        guiGraphics.pose().scale(scale);
                        guiGraphics.pose().translate(-slot.x, -slot.y);
                        this.renderSlot(guiGraphics, slot);
                    }else{
                        guiGraphics.pose().translate((customSlot.getWidth() - 18) / 2f, (customSlot.getHeight() - 18) / 2f);
                        this.renderSlot(guiGraphics, slot);
                    }
                    guiGraphics.pose().popMatrix();
                }
                if(this.hoveredSlot == slot && slot.isHighlightable())
                    helper.submitSprite(AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE, slot.x - 4, slot.y - 4, slotWidth + 6, slotHeight + 6);
            }else{
                // Regular slot
                if(this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)){
                    this.hoveredSlot = slot;
                    helper.submitSprite(AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
                }
                this.renderSlot(guiGraphics, slot);
                if(this.hoveredSlot == slot)
                    helper.submitSprite(AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
            }
        }

        // Render the widget's foreground
        this.widget.renderForeground(this.widgetRenderContext, helper, offsetMouseX, offsetMouseY);

        guiGraphics.pose().popMatrix();

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Call Architectury's client events
        if(CoreLib.isArchitecturyLoaded)
            ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.invoker().render(this, guiGraphics, mouseX, mouseY, partialTicks);

        this.renderCarriedItem(guiGraphics, mouseX, mouseY);
        this.renderSnapbackItem(guiGraphics);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(offsetX, offsetY);

        // Render the widget's overlay
        this.widget.renderOverlay(this.widgetRenderContext, helper, offsetMouseX, offsetMouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(this.widgetRenderContext, helper, offsetMouseX, offsetMouseY);

        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY){
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        return this.widget.mousePressed((int)mouseX - offsetX, (int)mouseY - offsetY, button, false) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        return this.widget.mouseReleased((int)mouseX - offsetX, (int)mouseY - offsetY, button, false) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        return this.widget.mouseScrolled((int)mouseX - offsetX, (int)mouseY - offsetY, verticalAmount, false) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
