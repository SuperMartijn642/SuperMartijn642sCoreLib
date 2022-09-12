package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.ContainerWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class WidgetContainerScreen<T extends Widget, X extends BaseContainer> extends ContainerScreen<X> {

    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("supermartijn642corelib", "textures/gui/slot.png");

    public static <T extends Widget, X extends BaseContainer> WidgetContainerScreen<T,X> of(T widget, X container, boolean drawSlots, boolean isPauseScreen){
        return new WidgetContainerScreen<>(widget, container, drawSlots, isPauseScreen);
    }

    public static <T extends Widget, X extends BaseContainer> WidgetContainerScreen<T,X> of(T widget, X container, boolean drawSlots){
        return new WidgetContainerScreen<>(widget, container, drawSlots);
    }

    protected final X container;
    protected final T widget;
    private boolean initialized = false;
    private final boolean drawSlots;
    private final boolean isPauseScreen;

    public WidgetContainerScreen(T widget, X container, boolean drawSlots, boolean isPauseScreen){
        super(container, container.player.inventory, TextComponents.empty().get());
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
    public void tick(){
        this.widget.update();
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(poseStack);

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int offsetMouseX = mouseX - offsetX;
        int offsetMouseY = mouseY - offsetY;
        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, 0);

        // Update whether the widget is focused
        this.widget.setFocused(offsetMouseX >= 0 && offsetMouseX < this.widget.width() && offsetMouseY >= 0 && offsetMouseY < this.widget.height());

        // Render the widget background
        this.widget.renderBackground(poseStack, offsetMouseX, offsetMouseY);

        if(this.drawSlots){
            for(Slot slot : this.container.slots){
                ScreenUtils.bindTexture(SLOT_TEXTURE);
                ScreenUtils.drawTexture(poseStack, slot.x - 1, slot.y - 1, 18, 18);
            }
        }

        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawBackground(this, poseStack, mouseX, mouseY));

        // Render the widget
        this.widget.render(poseStack, offsetMouseX, offsetMouseY);

        this.hoveredSlot = null;
        for(Slot slot : this.container.slots){
            if(!slot.isActive())
                continue;

            this.renderSlotOffset(poseStack, slot, offsetX, offsetY);
            if(this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)){
                this.hoveredSlot = slot;
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                int slotColor = this.getSlotColor(0);
                this.fillGradient(poseStack, slot.x, slot.y, slot.x + 16, slot.y + 16, slotColor, slotColor);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
            }
        }

        // Render the widget's foreground
        this.widget.renderForeground(poseStack, offsetMouseX, offsetMouseY);

        this.renderTooltip(poseStack, offsetMouseX, offsetMouseY);

        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(this, poseStack, mouseX, mouseY));

        ItemStack cursorStack = this.draggingItem.isEmpty() ? this.inventory.getCarried() : this.draggingItem;
        if(!cursorStack.isEmpty()){
            int offset = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if(!this.draggingItem.isEmpty() && this.isSplittingStack){
                cursorStack = cursorStack.copy();
                cursorStack.setCount(MathHelper.ceil(cursorStack.getCount() / 2f));
            }else if(this.isQuickCrafting && this.quickCraftSlots.size() > 1){
                cursorStack = cursorStack.copy();
                cursorStack.setCount(this.quickCraftingRemainder);
                if(cursorStack.isEmpty())
                    s = TextFormatting.YELLOW + "0";
            }

            this.renderFloatingItem(cursorStack, mouseX - 8, mouseY - offset, s);
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
            this.renderFloatingItem(this.snapbackItem, j1, k1, null);
        }

        // Render the widget's overlay
        this.widget.renderOverlay(poseStack, offsetMouseX, offsetMouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(poseStack, offsetMouseX, offsetMouseY);

        poseStack.popPose();
    }

    /**
     * Just a copy of {@link ContainerScreen#renderSlot(MatrixStack, Slot)} since the item rendering doesn't use the provided matrix stack 😑
     */
    private void renderSlotOffset(MatrixStack poseStack, Slot slot, int offsetX, int offsetY){
        int slotX = slot.x;
        int slotY = slot.y;

        ItemStack slotItem = slot.getItem();
        boolean drawHighlight = false;
        boolean drawItem = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack carried = this.inventory.getCarried();
        String countText = null;
        if(slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !slotItem.isEmpty()){
            slotItem = slotItem.copy();
            slotItem.setCount(slotItem.getCount() / 2);
        }else if(this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty()){
            if(this.quickCraftSlots.size() == 1)
                return;

            if(Container.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)){
                slotItem = carried.copy();
                drawHighlight = true;
                Container.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, slotItem, slot.getItem().isEmpty() ? 0 : slot.getItem().getCount());
                int k = Math.min(slotItem.getMaxStackSize(), slot.getMaxStackSize(slotItem));
                if(slotItem.getCount() > k){
                    countText = TextFormatting.YELLOW.toString() + k;
                    slotItem.setCount(k);
                }
            }else{
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100;
        if(slotItem.isEmpty() && slot.isActive()){
            Pair<ResourceLocation,ResourceLocation> pair = slot.getNoItemIcon();
            if(pair != null){
                TextureAtlasSprite sprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                this.minecraft.getTextureManager().bind(sprite.atlas().location());
                blit(poseStack, slotX, slotY, this.getBlitOffset(), 16, 16, sprite);
                drawItem = true;
            }
        }

        if(!drawItem){
            if(drawHighlight)
                fill(poseStack, slotX, slotY, slotX + 16, slotY + 16, -2130706433);

            // For some reason this part just ignores the given matrix stack, so we have to translate the position manually
            slotX += offsetX;
            slotY += offsetY;

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, slotItem, slotX, slotY);
            this.itemRenderer.renderGuiItemDecorations(this.font, slotItem, slotX, slotY, countText);
        }

        this.itemRenderer.blitOffset = 0;
        this.setBlitOffset(0);
    }

    @Override
    protected void renderBg(MatrixStack poseStack, float partialTicks, int mouseX, int mouseY){
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
    public boolean mouseScrolled(double mouseX, double mouseY, double amount){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        return this.widget.mouseScrolled((int)mouseX - offsetX, (int)mouseY - offsetY, amount, false) || super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(this.widget.keyPressed(keyCode, false))
            return true;

        InputMappings.Input key = InputMappings.getKey(keyCode, scanCode);
        if(ClientUtils.getMinecraft().options.keyInventory.isActiveAndMatches(key)){
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
    public String getNarrationMessage(){
        ITextComponent message = this.widget.getNarrationMessage();
        return message == null ? "" : TextComponents.fromTextComponent(message).format();
    }
}
