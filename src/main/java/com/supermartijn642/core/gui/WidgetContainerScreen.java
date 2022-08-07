package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.ContainerWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import static com.supermartijn642.core.gui.WidgetScreen.KEY_CODE_MAP;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class WidgetContainerScreen<T extends Widget, X extends BaseContainer> extends GuiContainer {

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
        super(container);
        this.widget = widget;
        this.container = container;
        this.drawSlots = drawSlots;
        this.isPauseScreen = isPauseScreen;
    }

    public WidgetContainerScreen(T widget, X container, boolean drawSlots){
        this(widget, container, drawSlots, false);
    }

    @Override
    public void setWorldAndResolution(Minecraft p_146280_1_, int p_146280_2_, int p_146280_3_){
        super.setWorldAndResolution(p_146280_1_, p_146280_2_, p_146280_3_);
    }

    @Override
    public void initGui(){
        if(!this.initialized){
            if(this.widget instanceof ContainerWidget<?>)
                //noinspection unchecked,rawtypes
                ((ContainerWidget)this.widget).initialize(this.container);
            else
                this.widget.initialize();
            this.initialized = true;
        }

        this.xSize = this.widget.width();
        this.ySize = this.widget.height();
        super.initGui();
    }

    @Override
    public void onGuiClosed(){
        this.widget.discard();
        super.onGuiClosed();
    }

    @Override
    public void updateScreen(){
        this.widget.update();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();

        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        int offsetMouseX = mouseX - offsetX;
        int offsetMouseY = mouseY - offsetY;
        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);

        // Update whether the widget is focused
        this.widget.setFocused(offsetMouseX >= 0 && offsetMouseX < this.widget.width() && offsetMouseY >= 0 && offsetMouseY < this.widget.height());

        // Render the widget background
        this.widget.renderBackground(offsetMouseX, offsetMouseY);

        if(this.drawSlots){
            for(Slot slot : this.container.inventorySlots){
                ScreenUtils.bindTexture(SLOT_TEXTURE);
                ScreenUtils.drawTexture(slot.xPos - 1, slot.yPos - 1, 18, 18);
            }
        }

        // Render the widget
        this.widget.render(offsetMouseX, offsetMouseY);

        for(Slot slot : this.container.inventorySlots){
            if(!slot.isEnabled())
                continue;

            slot.xPos += offsetX;
            slot.yPos += offsetY;
            this.drawSlot(slot);
            slot.xPos -= offsetX;
            slot.yPos -= offsetY;
            if(this.isMouseOverSlot(slot, offsetMouseX, offsetMouseY)){
                this.hoveredSlot = slot;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.colorMask(true, true, true, false);
                ScreenUtils.fillRect(slot.xPos, slot.yPos, 16, 16, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

        // Render the widget's foreground
        this.widget.renderForeground(offsetMouseX, offsetMouseY);

        GlStateManager.popMatrix();

        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(this, mouseX, mouseY));

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);

        ItemStack cursorStack = this.draggedStack.isEmpty() ? ClientUtils.getPlayer().inventory.getItemStack() : this.draggedStack;
        if(!cursorStack.isEmpty()){
            int offset = this.draggedStack.isEmpty() ? 8 : 16;
            String s = null;
            if(!this.draggedStack.isEmpty() && this.isRightMouseClick){
                cursorStack = cursorStack.copy();
                cursorStack.setCount(MathHelper.ceil(cursorStack.getCount() / 2f));
            }else if(this.dragSplitting && this.dragSplittingSlots.size() > 1){
                cursorStack = cursorStack.copy();
                cursorStack.setCount(this.dragSplittingRemnant);
                if(cursorStack.isEmpty())
                    s = TextFormatting.YELLOW + "0";
            }

            this.drawItemStack(cursorStack, mouseX - 8, mouseY - offset, s);
        }

        if(!this.returningStack.isEmpty()){
            float f = (float)(Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
            if(f >= 1.0F){
                f = 1.0F;
                this.returningStack = ItemStack.EMPTY;
            }

            int j2 = this.returningStackDestSlot.xPos - this.touchUpX;
            int k2 = this.returningStackDestSlot.yPos - this.touchUpY;
            int j1 = this.touchUpX + (int)(j2 * f);
            int k1 = this.touchUpY + (int)(k2 * f);
            this.drawItemStack(this.returningStack, j1, k1, null);
        }

        // Render the widget's overlay
        this.widget.renderOverlay(offsetMouseX, offsetMouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(offsetMouseX, offsetMouseY);

        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException{
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        if(!this.widget.mousePressed(mouseX, mouseY, button, false))
            super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        if(!this.widget.mouseReleased(mouseX, mouseY, button, false))
            super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();

        int mouseX = (int)((double)Mouse.getEventX() * this.width / this.mc.displayWidth - (this.width - this.widget.width()) / 2);
        int mouseY = (int)(this.height - (double)Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - (this.height - this.widget.height()) / 2);

        int scroll = Mouse.getEventDWheel() / 120;
        if(scroll != 0)
            this.mouseScrolled(mouseX, mouseY, scroll);
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount){
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        this.widget.mouseScrolled((int)mouseX, (int)mouseY, amount, false);
    }

    @Override
    public void handleKeyboardInput(){
        if(Keyboard.getEventKeyState()){
            char character = Keyboard.getEventCharacter();
            if(character >= ' ')
                this.charTyped(character);
            int key = Keyboard.getEventKey();
            if(key >= 0 && key < KEY_CODE_MAP.length && !this.keyPressed(KEY_CODE_MAP[key]))
                this.mc.dispatchKeypresses();
        }else{
            int key = Keyboard.getEventKey();
            if(key >= 0 && key < KEY_CODE_MAP.length && !this.keyReleased(KEY_CODE_MAP[key]))
                this.mc.dispatchKeypresses();
        }
    }

    public boolean keyPressed(int keyCode){
        if(this.widget.keyPressed(keyCode, false))
            return true;

        if(keyCode == 256 /* Escape */ || ClientUtils.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode)){
            this.closeScreen();
            return true;
        }

        return false;
    }

    public boolean keyReleased(int keyCode){
        return this.widget.keyReleased(keyCode, false);
    }

    public boolean charTyped(char character){
        return this.widget.charTyped(character, false);
    }

    @Override
    public boolean doesGuiPauseGame(){
        return this.isPauseScreen();
    }

    public boolean isPauseScreen(){
        return this.isPauseScreen;
    }

    protected void closeScreen(){
        ClientUtils.closeScreen();
    }
}
