package com.supermartijn642.core.gui.widget;

import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class TextFieldWidget extends Widget implements ITickableWidget {

    private String text;
    private String suggestion = "";
    protected int maxLength;
    private int cursorBlinkCounter;
    protected boolean focused;
    protected int lineScrollOffset;
    protected int cursorPosition;
    protected int selectionPos;
    protected boolean drawBackground = true;
    protected int activeTextColor = 14737632, inactiveTextColor = 7368816;

    private final BiConsumer<String,String> changeListener;

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength, BiConsumer<String,String> changeListener){
        super(x, y, width, height);
        this.text = defaultText;
        this.maxLength = maxLength;
        this.changeListener = changeListener;
    }

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength, Consumer<String> changeListener){
        this(x, y, width, height, defaultText, maxLength, changeListener == null ? null : (a, b) -> changeListener.accept(b));
    }

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength){
        this(x, y, width, height, defaultText, maxLength, (BiConsumer<String,String>)null);
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return new TextComponentTranslation("gui.narrate.editBox", this.suggestion, this.text);
    }

    @Override
    public void tick(){
        this.cursorBlinkCounter++;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        if(this.drawBackground)
            this.drawBackground();

        int textColor = this.active ? this.activeTextColor : this.inactiveTextColor;
        int relativeCursor = this.cursorPosition - this.lineScrollOffset;
        int relativeSelection = this.selectionPos - this.lineScrollOffset;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String s = fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.width - 8);
        boolean cursorInView = relativeCursor >= 0 && relativeCursor <= s.length();
        boolean shouldBlink = this.focused && this.cursorBlinkCounter / 8 % 2 == 0 && cursorInView;
        int left = this.x + 4;
        int top = this.y + (this.height - 8) / 2;
        int leftOffset = left;

        if(relativeSelection > s.length())
            relativeSelection = s.length();

        if(!s.isEmpty()){
            String s1 = cursorInView ? s.substring(0, relativeCursor) : s;
            leftOffset = fontRenderer.drawString(s1, left, top, textColor) + 1;
        }

        boolean cursorAtEnd = this.cursorPosition < this.text.length();
        int cursorX = leftOffset;

        if(!cursorInView)
            cursorX = relativeCursor > 0 ? left + this.width : left;
        else if(cursorAtEnd){
            cursorX = leftOffset - 1;
            leftOffset--;
        }

        // draw text
        if(!s.isEmpty() && cursorInView && relativeCursor < s.length())
            fontRenderer.drawString(s.substring(relativeCursor), leftOffset, top, textColor);

        // draw suggestion
        if(!this.suggestion.isEmpty() && this.text.isEmpty())
            fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(this.suggestion, this.width - 8 - fontRenderer.getStringWidth("...")) + "...", cursorX, top, -8355712);

        // draw cursor
        if(shouldBlink){
            if(cursorAtEnd)
                ScreenUtils.fillRect(cursorX - 0.5f, top - 1, 1, fontRenderer.FONT_HEIGHT, -3092272);
            else
                fontRenderer.drawStringWithShadow("_", cursorX, top, textColor);
        }

        if(relativeSelection != relativeCursor){
            int l1 = left + fontRenderer.getStringWidth(s.substring(0, relativeSelection));
            this.drawSelectionBox(cursorX, top - 1, l1 - 1, top + 1 + fontRenderer.FONT_HEIGHT);
        }
    }

    protected void drawBackground(){
        ScreenUtils.fillRect(this.x, this.y, this.width, this.height, this.focused ? -1 : -6250336);
        ScreenUtils.fillRect(this.x + 1, this.y + 1, this.width - 2, this.height - 2, -16777216);
    }

    protected void drawSelectionBox(int startX, int startY, int endX, int endY){
        if(startX < endX){
            int i = startX;
            startX = endX;
            endX = i;
        }

        if(startY < endY){
            int j = startY;
            startY = endY;
            endY = j;
        }

        if(endX > this.x + this.width){
            endX = this.x + this.width;
        }

        if(startX > this.x + this.width){
            startX = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0).endVertex();
        bufferbuilder.pos(endX, endY, 0).endVertex();
        bufferbuilder.pos(endX, startY, 0).endVertex();
        bufferbuilder.pos(startX, startY, 0).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void clear(){
        this.setText("");
    }

    public void setText(String text){
        String oldText = this.text;

        this.setTextSuppressed(text);

        if(!oldText.equals(this.text))
            this.onTextChanged(oldText, text);
    }

    public String getText(){
        return this.text;
    }

    /**
     * Sets {@code text} without calling {@link TextFieldWidget#onTextChanged(String, String)}
     */
    public void setTextSuppressed(String text){
        if(text == null)
            text = "";
        else if(text.length() > this.maxLength)
            text = ChatAllowedCharacters.filterAllowedCharacters(text.substring(0, this.maxLength));

        this.lineScrollOffset = 0;
        this.cursorPosition = 0;
        this.selectionPos = 0;
        this.text = text;
    }

    protected void addTextAtCursor(String text){
        String oldText = this.text;

        text = ChatAllowedCharacters.filterAllowedCharacters(text);
        if(text.length() + this.text.length() - this.getSelectedText().length() > this.maxLength)
            text = text.substring(0, this.maxLength - this.text.length() + this.getSelectedText().length() + 1);

        int min = Math.min(this.cursorPosition, this.selectionPos);
        int max = Math.max(this.cursorPosition, this.selectionPos);
        this.text = this.text.substring(0, min) + text + this.text.substring(max);
        this.cursorPosition = min + text.length();
        this.selectionPos = this.cursorPosition;
        this.moveLineOffsetToCursor();

        if(!oldText.equals(this.text)){
            this.cursorBlinkCounter = 1;
            this.onTextChanged(oldText, this.text);
        }
    }

    protected void removeAtCursor(boolean left){
        if(this.text.isEmpty())
            return;

        String oldText = text;
        if(this.cursorPosition != this.selectionPos){
            this.text = this.text.substring(0, Math.min(this.cursorPosition, this.selectionPos)) + this.text.substring(Math.max(this.cursorPosition, this.selectionPos));
            this.cursorPosition = this.selectionPos = Math.min(this.cursorPosition, this.selectionPos);
        }else if(left && this.cursorPosition > 0){
            this.text = this.text.substring(0, this.cursorPosition - 1) + this.text.substring(this.cursorPosition);
            this.cursorPosition -= 1;
            this.selectionPos -= 1;
        }else if(!left && this.cursorPosition < this.text.length())
            this.text = this.text.substring(0, this.cursorPosition) + this.text.substring(this.cursorPosition + 1);

        this.moveLineOffsetToCursor();

        this.cursorBlinkCounter = 1;

        this.onTextChanged(oldText, this.text);
    }

    protected void moveLineOffsetToCursor(){
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int availableWidth = this.width - 8 - (this.cursorPosition == this.text.length() ? fontRenderer.getStringWidth("_") : 0);
        int min = Math.min(this.cursorPosition + 1, this.text.length()) - fontRenderer.trimStringToWidth(new StringBuilder(this.text.substring(0, Math.min(this.text.length(), this.cursorPosition + 2))).reverse().toString(), availableWidth).length();
        int max = Math.max(this.cursorPosition - 1, 0) + fontRenderer.trimStringToWidth(this.text.substring(Math.max(this.cursorPosition - 1, 0)), availableWidth).length();
        max = max - fontRenderer.trimStringToWidth(new StringBuilder(this.text.substring(0, max)).reverse().toString(), availableWidth).length();
        this.lineScrollOffset = Math.min(Math.max(this.lineScrollOffset, min), max);
    }

    public String getSelectedText(){
        if(this.cursorPosition == this.selectionPos)
            return "";

        return this.text.substring(Math.min(this.cursorPosition, this.selectionPos), Math.max(this.cursorPosition, this.selectionPos));
    }

    protected void onTextChanged(String oldText, String newText){
        if(this.changeListener != null)
            this.changeListener.accept(oldText, newText);
    }

    public void setSuggestion(String suggestion){
        this.suggestion = suggestion == null ? "" : suggestion;
    }

    public String getSuggestion(){
        return this.suggestion;
    }

    public void setTextColors(int activeTextColor, int inactiveTextColor){
        this.activeTextColor = activeTextColor;
        this.inactiveTextColor = inactiveTextColor;
    }

    public void setDrawBackground(boolean drawBackground){
        this.drawBackground = drawBackground;
    }

    public boolean isFocused(){
        return this.focused;
    }

    public void setFocused(boolean focused){
        this.focused = focused;
    }

    @Override
    public void keyPressed(int keyCode){
        if(!this.canWrite())
            return;

        boolean shift = GuiScreen.isShiftKeyDown();
        if(keyCode == 1){
            this.setFocused(false);
        }else if(GuiScreen.isKeyComboCtrlA(keyCode)){
            this.lineScrollOffset = 0;
            this.cursorPosition = this.text.length();
            this.selectionPos = 0;
        }else if(GuiScreen.isKeyComboCtrlC(keyCode)){
            GuiScreen.setClipboardString(this.getSelectedText());
        }else if(GuiScreen.isKeyComboCtrlV(keyCode)){
            this.addTextAtCursor(GuiScreen.getClipboardString());
        }else if(GuiScreen.isKeyComboCtrlX(keyCode)){
            GuiScreen.setClipboardString(this.getSelectedText());
            this.addTextAtCursor("");
        }else{
            switch(keyCode){
                case 14: // backspace
                    this.removeAtCursor(true);
                    break;
                case 210: // insert
                case 264: // ?
                case 265: // ?
                case 201: // page up
                case 209: // page down
                default:
                    break;
                case 211: // delete
                    this.removeAtCursor(false);
                    break;
                case 205: // right
                    if(!shift && this.cursorPosition != this.selectionPos)
                        this.cursorPosition = this.selectionPos = Math.max(this.cursorPosition, this.selectionPos);
                    else if(this.cursorPosition < this.text.length()){
                        this.cursorPosition = this.cursorPosition + 1;
                        if(!shift)
                            this.selectionPos = this.cursorPosition;
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 203: // left
                    if(!shift && this.cursorPosition != this.selectionPos)
                        this.cursorPosition = this.selectionPos = Math.min(this.cursorPosition, this.selectionPos);
                    else if(this.cursorPosition > 0){
                        this.cursorPosition = this.cursorPosition - 1;
                        if(!shift)
                            this.selectionPos = this.cursorPosition;
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 199: // home
                    this.cursorPosition = this.selectionPos = 0;
                    this.moveLineOffsetToCursor();
                    break;
                case 207: // end
                    this.cursorPosition = this.selectionPos = this.text.length();
                    this.moveLineOffsetToCursor();
                    break;
            }
        }
    }

    @Override
    public void charTyped(char c){
        if(!this.canWrite())
            return;

        if(ChatAllowedCharacters.isAllowedCharacter(c))
            this.addTextAtCursor(Character.toString(c));
    }

    public boolean canWrite(){
        return this.active && this.focused;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button){
        this.focused = this.active && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;

        if(this.focused){
            if(button == 1)
                this.clear();
            else{
                int offset = MathHelper.floor(mouseX) - this.x - 4;

                FontRenderer font = Minecraft.getMinecraft().fontRenderer;
                String s = font.trimStringToWidth(this.text.substring(this.lineScrollOffset), Math.min(offset, this.width - 8));
                this.cursorPosition = s.length() + this.lineScrollOffset;
                if(!GuiScreen.isShiftKeyDown())
                    this.selectionPos = this.cursorPosition;
            }
        }
    }
}
