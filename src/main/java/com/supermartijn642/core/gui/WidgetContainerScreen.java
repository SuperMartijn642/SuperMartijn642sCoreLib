package com.supermartijn642.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.ContainerWidget;
import com.supermartijn642.core.gui.widget.MutableWidgetRenderContext;
import com.supermartijn642.core.gui.widget.Widget;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class WidgetContainerScreen<T extends Widget, X extends BaseContainer> extends AbstractContainerScreen<X> {

    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "textures/gui/slot.png");

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
        this.widgetRenderContext.update(guiGraphics, partialTicks);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Call Architectury's client events
        if(CoreLib.isArchitecturyLoaded)
            ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.invoker().render(this, guiGraphics, mouseX, mouseY, partialTicks);

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int offsetMouseX = mouseX - offsetX;
        int offsetMouseY = mouseY - offsetY;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(offsetX, offsetY, 0);
        RenderSystem.disableDepthTest();

        // Update whether the widget is focused
        this.widget.setFocused(offsetMouseX >= 0 && offsetMouseX < this.widget.width() && offsetMouseY >= 0 && offsetMouseY < this.widget.height());

        // Render the widget background
        this.widget.renderBackground(this.widgetRenderContext, offsetMouseX, offsetMouseY);

        if(this.drawSlots){
            for(Slot slot : this.container.slots){
                ScreenUtils.bindTexture(SLOT_TEXTURE);
                ScreenUtils.drawTexture(guiGraphics.pose(), slot.x - 1, slot.y - 1, 18, 18);
            }
        }

        // Render the widget
        this.widget.render(this.widgetRenderContext, offsetMouseX, offsetMouseY);

        this.hoveredSlot = null;
        for(Slot slot : this.container.slots){
            if(!slot.isActive())
                continue;

            this.renderSlot(guiGraphics, slot);
            if(this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)){
                this.hoveredSlot = slot;
                renderSlotHighlight(guiGraphics, slot.x, slot.y, 0);
            }
        }

        // Render the widget's foreground
        this.widget.renderForeground(this.widgetRenderContext, offsetMouseX, offsetMouseY);

        this.renderTooltip(guiGraphics, offsetMouseX, offsetMouseY);

        // Call Architectury's client events
        if(CoreLib.isArchitecturyLoaded)
            ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.invoker().render(this, guiGraphics, mouseX, mouseY, partialTicks);

        ItemStack cursorStack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if(!cursorStack.isEmpty()){
            int offset = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if(!this.draggingItem.isEmpty() && this.isSplittingStack){
                cursorStack = cursorStack.copy();
                cursorStack.setCount(Mth.ceil(cursorStack.getCount() / 2f));
            }else if(this.isQuickCrafting && this.quickCraftSlots.size() > 1){
                cursorStack = cursorStack.copy();
                cursorStack.setCount(this.quickCraftingRemainder);
                if(cursorStack.isEmpty())
                    s = ChatFormatting.YELLOW + "0";
            }

            this.renderFloatingItem(guiGraphics, cursorStack, offsetMouseX - 8, offsetMouseY - offset, s);
        }

        if(!this.snapbackItem.isEmpty()){
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if(f >= 1.0F){
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int j2 = this.snapbackEnd.x - this.snapbackStartX;
            int k2 = this.snapbackEnd.y - this.snapbackStartY;
            int j1 = this.snapbackStartX + (int)(j2 * f);
            int k1 = this.snapbackStartY + (int)(k2 * f);
            this.renderFloatingItem(guiGraphics, this.snapbackItem, j1, k1, null);
        }

        // Render the widget's overlay
        this.widget.renderOverlay(this.widgetRenderContext, offsetMouseX, offsetMouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(this.widgetRenderContext, offsetMouseX, offsetMouseY);

        guiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
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
        return this.widget.mouseScrolled((int)mouseX - offsetX, (int)mouseY - offsetY, horizontalAmount, false) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
