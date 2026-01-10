package com.supermartijn642.core.gui;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.ContainerWidget;
import com.supermartijn642.core.gui.widget.MutableWidgetRenderContext;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

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
    private boolean dragging = false;

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

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int offsetMouseX = mouseX - offsetX;
        int offsetMouseY = mouseY - offsetY;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(offsetX, offsetY);

        // Update whether the widget is focused
        if(!this.dragging)
            this.widget.setFocused(offsetMouseX >= 0 && offsetMouseX < this.widget.width() && offsetMouseY >= 0 && offsetMouseY < this.widget.height());

        // Update cursor
        CursorType curser = this.widget.curser(offsetMouseX, offsetMouseY);
        if(curser != null)
            guiGraphics.requestCursor(curser);

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
                        this.renderSlot(guiGraphics, slot, mouseX, mouseY);
                    }else{
                        guiGraphics.pose().translate((customSlot.getWidth() - 18) / 2f, (customSlot.getHeight() - 18) / 2f);
                        this.renderSlot(guiGraphics, slot, mouseX, mouseY);
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
                this.renderSlot(guiGraphics, slot, mouseX, mouseY);
                if(this.hoveredSlot == slot)
                    helper.submitSprite(AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
            }
        }

        // Render the widget's foreground
        this.widget.renderForeground(this.widgetRenderContext, helper, offsetMouseX, offsetMouseY);

        NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, guiGraphics, mouseX, mouseY));

        guiGraphics.pose().popMatrix();

        this.renderTooltip(guiGraphics, mouseX, mouseY);

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
