package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class WidgetScreen<T extends Widget> extends GuiScreen {

    /**
     * An array to convert {@link Keyboard} keys to GLFW 3 key codes
     */
    static final int[] KEY_CODE_MAP = {
        -1, 256, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 61, 259, 258, 81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 91, 93, 257, 341, 65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 39, 96, 340, 92, 90, 88, 67, 86, 66, 78, 77, 44, 46, 47, 344, 332, 342, 32, 280, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 282, 281, 327, 328, 329, 333, 324, 325, 326, 334, 321, 322, 323, 320, 330, 300, 301, 302, 303, 304, 305, 306, 307, -1, 308, -1, -1, -1, 336, -1, -1, -1, -1, -1, -1, -1, -1, 335, 345, -1, 44, 331, 348, 342, -1, 284, 268, 265, 266, 263, 262, 269, 264, 267, 260, 261, -1, 343, 347, -1, -1, -1
    };

    public static <T extends Widget> WidgetScreen<T> of(T widget){
        return new WidgetScreen<>(widget);
    }

    public static <T extends Widget> WidgetScreen<T> of(T widget, boolean isPauseScreen){
        return new WidgetScreen<>(widget, isPauseScreen);
    }

    protected final T widget;
    private boolean initialized = false;
    private boolean isPauseScreen = false;

    public WidgetScreen(T widget, boolean isPauseScreen){
        this.widget = widget;
        this.isPauseScreen = isPauseScreen;
    }

    public WidgetScreen(T widget){
        this(widget, false);
    }

    @Override
    public void initGui(){
        if(!this.initialized){
            this.widget.initialize();
            this.initialized = true;
        }
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
        mouseX -= offsetX;
        mouseY -= offsetY;
        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);

        // Update whether the widget is focused
        this.widget.setFocused(mouseX >= 0 && mouseX < this.widget.width() && mouseY >= 0 && mouseY < this.widget.height());

        // Render the widget background
        this.widget.renderBackground(mouseX, mouseY);
        // Render the widget
        this.widget.render(mouseX, mouseY);
        // Render the widget's foreground
        this.widget.renderForeground(mouseX, mouseY);
        // Render the widget's overlay
        this.widget.renderOverlay(mouseX, mouseY);
        // Render the widget's tooltips
        this.widget.renderTooltips(mouseX, mouseY);

        GlStateManager.popMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException{
        int offsetX = (this.width - this.widget.width()) / 2, offsetY = (this.height - this.widget.height()) / 2;
        mouseX -= offsetX;
        mouseY -= offsetY;
        if(!this.widget.mousePressed(mouseX - offsetX, mouseY - offsetY, button, false))
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

        if(keyCode == 256 /* Escape */ || ClientUtils.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(Keyboard.getEventKey())){
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
