package com.supermartijn642.core.gui.widget.premade;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.CursorTypes;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class TextFieldWidget extends BaseWidget {

    private String text;
    private String suggestion = "";
    protected int maxLength;
    private int cursorBlinkCounter;
    protected boolean selected;
    private boolean active = true;
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
        this.cursorPosition = this.selectionPos = defaultText.length();
    }

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength, Consumer<String> changeListener){
        this(x, y, width, height, defaultText, maxLength, changeListener == null ? null : (a, b) -> changeListener.accept(b));
    }

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength){
        this(x, y, width, height, defaultText, maxLength, (BiConsumer<String,String>)null);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("gui.narrate.editBox", this.suggestion, this.text).get();
    }

    @Override
    public void update(){
        this.cursorBlinkCounter++;
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(this.drawBackground)
            this.drawBackground(graphics);

        int textColor = this.active ? this.activeTextColor : this.inactiveTextColor;
        int relativeCursor = this.cursorPosition - this.lineScrollOffset;
        int relativeSelection = this.selectionPos - this.lineScrollOffset;
        Font fontRenderer = context.font();
        String s = fontRenderer.plainSubstrByWidth(this.text.substring(this.lineScrollOffset), this.width - 8);
        boolean cursorInView = relativeCursor >= 0 && relativeCursor <= s.length();
        boolean shouldBlink = this.selected && this.cursorBlinkCounter / 8 % 2 == 0 && cursorInView;
        int left = this.x + 4;
        int top = this.y + (this.height - 8) / 2;
        int leftOffset = left;

        if(relativeSelection > s.length())
            relativeSelection = s.length();

        if(!s.isEmpty()){
            String s1 = cursorInView ? s.substring(0, relativeCursor) : s;
            graphics.submitText(s1, left, top, p -> p.color(textColor));
            leftOffset += fontRenderer.width(s1) + 1;
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
            graphics.submitText(s.substring(relativeCursor), leftOffset, top, p -> p.color(textColor));

        // draw suggestion
        if(!this.suggestion.isEmpty() && this.text.isEmpty())
            graphics.submitText(this.suggestion, cursorX, top, p -> p.cutoffWithDots(this.width - 8).color(-8355712));

        // draw cursor
        if(shouldBlink){
            if(cursorAtEnd)
                graphics.submitRectangle(cursorX - 0.5f, top - 1, 1, fontRenderer.lineHeight, p -> p.color(-3092272));
            else
                graphics.submitText("_", cursorX, top, p -> p.color(textColor));
        }

        if(relativeSelection != relativeCursor){
            int l1 = left + fontRenderer.width(s.substring(0, relativeSelection));
            this.drawSelectionBox(graphics, cursorX, top - 1, l1 - 1, top + 1 + fontRenderer.lineHeight);
        }
    }

    protected void drawBackground(GuiGraphicsHelper graphics){
        graphics.submitRectangle(this.x, this.y, this.width, this.height, p -> p.color(this.selected ? -1 : -6250336));
        graphics.submitRectangle(this.x + 1, this.y + 1, this.width - 2, this.height - 2, p -> p.color(-16777216));
    }

    protected void drawSelectionBox(GuiGraphicsHelper graphics, int startX, int startY, int endX, int endY){
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

        if(endX > this.x + this.width)
            endX = this.x + this.width;

        if(startX > this.x + this.width)
            startX = this.x + this.width;

        graphics.submitRectangle(startX, startY, endX - startX, endY - startY, p -> p.color(-16776961).renderPipeline(RenderPipelines.GUI_TEXT_HIGHLIGHT));
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
            text = StringUtil.filterText(text.substring(0, this.maxLength));

        this.lineScrollOffset = 0;
        this.cursorPosition = 0;
        this.selectionPos = 0;
        this.text = text;
    }

    protected void addTextAtCursor(String text){
        String oldText = this.text;

        text = StringUtil.filterText(text);
        if(text.length() + this.text.length() - this.getSelectedText().length() > this.maxLength)
            text = text.substring(0, this.maxLength - this.text.length() + this.getSelectedText().length());

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

    protected void removeAtCursor(boolean left, boolean wholeWord){
        if(this.text.isEmpty())
            return;

        if(this.cursorPosition == this.selectionPos){
            if(wholeWord)
                this.selectionPos = this.getEndOfWord(this.cursorPosition, left);
            else if(left && this.cursorPosition > 0)
                this.selectionPos = this.cursorPosition - 1;
            else if(!left && this.cursorPosition < this.text.length())
                this.selectionPos = this.cursorPosition + 1;
        }

        String oldText = this.text;
        if(this.cursorPosition != this.selectionPos){
            this.text = this.text.substring(0, Math.min(this.cursorPosition, this.selectionPos)) + this.text.substring(Math.max(this.cursorPosition, this.selectionPos));
            this.cursorPosition = this.selectionPos = Math.min(this.cursorPosition, this.selectionPos);
        }

        this.moveLineOffsetToCursor();

        this.cursorBlinkCounter = 1;

        this.onTextChanged(oldText, this.text);
    }

    protected int getEndOfWord(int fromIndex, boolean left){
        int index = Math.clamp(fromIndex, 0, this.text.length());
        if(left){
            while(index > 0 && this.text.charAt(index - 1) == ' ')
                index--;
            while(index > 0 && this.text.charAt(index) != ' ')
                index--;
        }else{
            while(index < this.text.length() && this.text.charAt(index) == ' ')
                index++;
            while(index < this.text.length() && this.text.charAt(index) != ' ')
                index++;
        }
        return index;
    }

    protected void moveLineOffsetToCursor(){
        Font fontRenderer = ClientUtils.getFontRenderer();
        int availableWidth = this.width - 8 - (this.cursorPosition == this.text.length() ? fontRenderer.width("_") : 0);
        int min = Math.min(this.cursorPosition + 1, this.text.length()) - fontRenderer.plainSubstrByWidth(new StringBuilder(this.text.substring(0, Math.min(this.text.length(), this.cursorPosition + 2))).reverse().toString(), availableWidth).length();
        int max = Math.max(this.cursorPosition - 1, 0) + fontRenderer.plainSubstrByWidth(this.text.substring(Math.max(this.cursorPosition - 1, 0)), availableWidth).length();
        max = max - fontRenderer.plainSubstrByWidth(new StringBuilder(this.text.substring(0, max)).reverse().toString(), availableWidth).length();
        this.lineScrollOffset = Math.min(Math.max(this.lineScrollOffset, min), max);
    }

    /**
     * Get the text offset index for the given mouse position.
     */
    protected int getMousePositionIndex(int mouseX, int mouseY){
        int offset = Mth.floor(mouseX) - this.x - 4;
        String s = ClientUtils.getFontRenderer().plainSubstrByWidth(this.text.substring(this.lineScrollOffset), Math.min(offset, this.width - 8));
        return s.length() + this.lineScrollOffset;
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

    public boolean isSelected(){
        return this.selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public void setActive(boolean active){
        this.active = active;
        if(!active)
            this.setSelected(false);
    }

    @Override
    public CursorType curser(int mouseX, int mouseY){
        return this.active ? CursorTypes.iBeam() : null;
    }

    @Override
    public boolean keyPressed(KeyEvent event, boolean hasBeenHandled){
        if(hasBeenHandled || !this.canWrite() || !this.selected)
            return false;

        boolean shift = event.hasShiftDown();
        if(event.isEscape()){
            this.setSelected(false);
        }else if(event.isSelectAll()){
            this.lineScrollOffset = 0;
            this.cursorPosition = this.text.length();
            this.selectionPos = 0;
        }else if(event.isCopy()){
            ClientUtils.getMinecraft().keyboardHandler.setClipboard(this.getSelectedText());
        }else if(event.isPaste()){
            this.addTextAtCursor(ClientUtils.getMinecraft().keyboardHandler.getClipboard());
        }else if(event.isCut()){
            ClientUtils.getMinecraft().keyboardHandler.setClipboard(this.getSelectedText());
            this.addTextAtCursor("");
        }else{
            switch(event.key()){
                case 259: // backspace
                    this.removeAtCursor(true, event.hasControlDown());
                    break;
                case 260: // insert
                case 264: // down
                case 265: // up
                case 266: // page up
                case 267: // page down
                default:
                    return true;
                case 261: // delete
                    this.removeAtCursor(false, event.hasControlDown());
                    break;
                case 262: // right
                    if(!shift && this.cursorPosition != this.selectionPos)
                        this.cursorPosition = this.selectionPos = Math.max(this.cursorPosition, this.selectionPos);
                    else if(this.cursorPosition < this.text.length()){
                        if(event.hasControlDown())
                            this.cursorPosition = this.getEndOfWord(this.cursorPosition, false);
                        else
                            this.cursorPosition = this.cursorPosition + 1;
                        if(!shift)
                            this.selectionPos = this.cursorPosition;
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 263: // left
                    if(!shift && this.cursorPosition != this.selectionPos)
                        this.cursorPosition = this.selectionPos = Math.min(this.cursorPosition, this.selectionPos);
                    else if(this.cursorPosition > 0){
                        if(event.hasControlDown())
                            this.cursorPosition = this.getEndOfWord(this.cursorPosition, true);
                        else
                            this.cursorPosition = this.cursorPosition - 1;
                        if(!shift)
                            this.selectionPos = this.cursorPosition;
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 268: // home
                    this.cursorPosition = 0;
                    if(!shift)
                        this.selectionPos = this.cursorPosition;
                    this.moveLineOffsetToCursor();
                    break;
                case 269: // end
                    this.cursorPosition = this.text.length();
                    if(!shift)
                        this.selectionPos = this.cursorPosition;
                    this.moveLineOffsetToCursor();
                    break;
            }

            return true;
        }

        return true;
    }

    @Override
    public boolean charTyped(char character, boolean hasBeenHandled){
        if(hasBeenHandled || !this.canWrite())
            return false;

        if(StringUtil.isAllowedChatCharacter(character))
            this.addTextAtCursor(Character.toString(character));

        return true;
    }

    public boolean canWrite(){
        return this.active && this.selected;
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled){
        if(!hasBeenHandled && this.active && this.isHovered(mouseX, mouseY)){
            this.setSelected(true);
            if(info.button() == 1) // Right-click
                this.clear();
            else if(info.button() == 0){ // Left-click
                if(isDoubleClick){
                    int from = this.cursorPosition > 0 && this.text.charAt(this.cursorPosition - 1) != ' ' ?
                        this.getEndOfWord(this.cursorPosition, true) : this.cursorPosition;
                    int to = this.cursorPosition < this.text.length() && this.text.charAt(this.cursorPosition) != ' ' ?
                        this.getEndOfWord(this.cursorPosition, true) : this.cursorPosition;
                    this.selectionPos = from;
                    this.cursorPosition = to;
                }else{
                    this.cursorPosition = this.getMousePositionIndex(mouseX, mouseY);
                    if(!info.hasShiftDown())
                        this.selectionPos = this.cursorPosition;
                }
            }
            return true;
        }else
            this.setSelected(false);

        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, MouseButtonInfo info, double deltaX, double deltaY, boolean hasBeenHandled){
        if(!hasBeenHandled && this.active && this.isFocused() && info.button() == 0){
            this.setSelected(true);
            this.cursorPosition = this.getMousePositionIndex(mouseX, mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, info, deltaX, deltaY, hasBeenHandled);
    }

    private boolean isHovered(int mouseX, int mouseY){
        return this.x <= mouseX && this.x + this.width > mouseX && this.y <= mouseY && this.y + this.height > mouseY;
    }
}
