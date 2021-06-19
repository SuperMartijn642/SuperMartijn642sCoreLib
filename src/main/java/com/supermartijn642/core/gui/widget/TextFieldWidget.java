package com.supermartijn642.core.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

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
        this.cursorPosition = this.selectionPos = defaultText.length();
    }

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength, Consumer<String> changeListener){
        this(x, y, width, height, defaultText, maxLength, changeListener == null ? null : (a, b) -> changeListener.accept(b));
    }

    public TextFieldWidget(int x, int y, int width, int height, String defaultText, int maxLength){
        this(x, y, width, height, defaultText, maxLength, (BiConsumer<String,String>)null);
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return TextComponents.translation("gui.narrate.editBox", this.suggestion, this.text).get();
    }

    @Override
    public void tick(){
        this.cursorBlinkCounter++;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(this.drawBackground)
            this.drawBackground(matrixStack);

        int textColor = this.active ? this.activeTextColor : this.inactiveTextColor;
        int relativeCursor = this.cursorPosition - this.lineScrollOffset;
        int relativeSelection = this.selectionPos - this.lineScrollOffset;
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        String s = fontRenderer.func_238412_a_(this.text.substring(this.lineScrollOffset), this.width - 8);
        boolean cursorInView = relativeCursor >= 0 && relativeCursor <= s.length();
        boolean shouldBlink = this.focused && this.cursorBlinkCounter / 8 % 2 == 0 && cursorInView;
        int left = this.x + 4;
        int top = this.y + (this.height - 8) / 2;
        int leftOffset = left;

        if(relativeSelection > s.length())
            relativeSelection = s.length();

        if(!s.isEmpty()){
            String s1 = cursorInView ? s.substring(0, relativeCursor) : s;
            leftOffset = fontRenderer.drawString(matrixStack, s1, left, top, textColor) + 1;
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
            fontRenderer.drawString(matrixStack, s.substring(relativeCursor), leftOffset, top, textColor);

        // draw suggestion
        if(!this.suggestion.isEmpty() && this.text.isEmpty())
            fontRenderer.drawStringWithShadow(matrixStack, fontRenderer.func_238412_a_(this.suggestion, this.width - 8 - fontRenderer.getStringWidth("...")) + "...", cursorX, top, -8355712);

        // draw cursor
        if(shouldBlink){
            if(cursorAtEnd)
                ScreenUtils.fillRect(matrixStack, cursorX - 0.5f, top - 1, 1, fontRenderer.FONT_HEIGHT, -3092272);
            else
                fontRenderer.drawStringWithShadow(matrixStack, "_", cursorX, top, textColor);
        }

        if(relativeSelection != relativeCursor){
            int l1 = left + fontRenderer.getStringWidth(s.substring(0, relativeSelection));
            this.drawSelectionBox(matrixStack, cursorX, top - 1, l1 - 1, top + 1 + fontRenderer.FONT_HEIGHT);
        }
    }

    protected void drawBackground(MatrixStack matrixStack){
        ScreenUtils.fillRect(matrixStack, this.x, this.y, this.width, this.height, this.focused ? -1 : -6250336);
        ScreenUtils.fillRect(matrixStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2, -16777216);
    }

    protected void drawSelectionBox(MatrixStack matrixStack, int startX, int startY, int endX, int endY){
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

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(matrix, startX, endY, 0).endVertex();
        bufferbuilder.pos(matrix, endX, endY, 0).endVertex();
        bufferbuilder.pos(matrix, endX, startY, 0).endVertex();
        bufferbuilder.pos(matrix, startX, startY, 0).endVertex();
        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
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
            text = SharedConstants.filterAllowedCharacters(text.substring(0, this.maxLength));

        this.lineScrollOffset = 0;
        this.cursorPosition = 0;
        this.selectionPos = 0;
        this.text = text;
    }

    protected void addTextAtCursor(String text){
        String oldText = this.text;

        text = SharedConstants.filterAllowedCharacters(text);
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
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int availableWidth = this.width - 8 - (this.cursorPosition == this.text.length() ? fontRenderer.getStringWidth("_") : 0);
        int min = Math.min(this.cursorPosition + 1, this.text.length()) - fontRenderer.func_238412_a_(new StringBuilder(this.text.substring(0, Math.min(this.text.length(), this.cursorPosition + 2))).reverse().toString(), availableWidth).length();
        int max = Math.max(this.cursorPosition - 1, 0) + fontRenderer.func_238412_a_(this.text.substring(Math.max(this.cursorPosition - 1, 0)), availableWidth).length();
        max = max - fontRenderer.func_238412_a_(new StringBuilder(this.text.substring(0, max)).reverse().toString(), availableWidth).length();
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

        boolean shift = Screen.hasShiftDown();
        if(keyCode == 256){
            this.setFocused(false);
        }else if(Screen.isSelectAll(keyCode)){
            this.lineScrollOffset = 0;
            this.cursorPosition = this.text.length();
            this.selectionPos = 0;
        }else if(Screen.isCopy(keyCode)){
            Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
        }else if(Screen.isPaste(keyCode)){
            this.addTextAtCursor(Minecraft.getInstance().keyboardListener.getClipboardString());
        }else if(Screen.isCut(keyCode)){
            Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
            this.addTextAtCursor("");
        }else{
            switch(keyCode){
                case 259: // backspace
                    this.removeAtCursor(true);
                    break;
                case 260: // insert
                case 264: // ?
                case 265: // ?
                case 266: // page up
                case 267: // page down
                default:
                    break;
                case 261: // delete
                    this.removeAtCursor(false);
                    break;
                case 262: // right
                    if(!shift && this.cursorPosition != this.selectionPos)
                        this.cursorPosition = this.selectionPos = Math.max(this.cursorPosition, this.selectionPos);
                    else if(this.cursorPosition < this.text.length()){
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
                        this.cursorPosition = this.cursorPosition - 1;
                        if(!shift)
                            this.selectionPos = this.cursorPosition;
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 268: // home
                    this.cursorPosition = this.selectionPos = 0;
                    this.moveLineOffsetToCursor();
                    break;
                case 269: // end
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

        if(SharedConstants.isAllowedCharacter(c))
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

                FontRenderer font = Minecraft.getInstance().fontRenderer;
                String s = font.func_238412_a_(this.text.substring(this.lineScrollOffset), Math.min(offset, this.width - 8));
                this.cursorPosition = s.length() + this.lineScrollOffset;
                if(!Screen.hasShiftDown())
                    this.selectionPos = this.cursorPosition;
            }
        }
    }
}
