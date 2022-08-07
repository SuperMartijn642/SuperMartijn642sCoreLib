package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class ScreenUtils {

    private static final ResourceLocation BUTTON_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/buttons.png");
    private static final ResourceLocation SCREEN_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/background.png");

    public static final int DEFAULT_TEXT_COLOR = 4210752, ACTIVE_TEXT_COLOR = 14737632, INACTIVE_TEXT_COLOR = 7368816;

    public static void drawString(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawString(fontRenderer, text.getFormattedText(), x, y, color);
    }

    public static void drawString(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawString(fontRenderer, text.getFormattedText(), x, y);
    }

    public static void drawString(ITextComponent text, float x, float y, int color){
        drawString(text.getFormattedText(), x, y, color);
    }

    public static void drawString(ITextComponent text, float x, float y){
        drawString(text.getFormattedText(), x, y);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawStringWithShadow(fontRenderer, text.getFormattedText(), x, y, color);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawStringWithShadow(fontRenderer, text.getFormattedText(), x, y);
    }

    public static void drawStringWithShadow(ITextComponent text, float x, float y, int color){
        drawStringWithShadow(text.getFormattedText(), x, y, color);
    }

    public static void drawStringWithShadow(ITextComponent text, float x, float y){
        drawStringWithShadow(text.getFormattedText(), x, y);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawCenteredString(fontRenderer, text.getFormattedText(), x, y, color);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawCenteredString(fontRenderer, text.getFormattedText(), x, y);
    }

    public static void drawCenteredString(ITextComponent text, float x, float y, int color){
        drawCenteredString(text.getFormattedText(), x, y, color);
    }

    public static void drawCenteredString(ITextComponent text, float x, float y){
        drawCenteredString(text.getFormattedText(), x, y);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(fontRenderer, text.getFormattedText(), x, y, color);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(fontRenderer, text.getFormattedText(), x, y);
    }

    public static void drawCenteredStringWithShadow(ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(text.getFormattedText(), x, y, color);
    }

    public static void drawCenteredStringWithShadow(ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(text.getFormattedText(), x, y);
    }

    public static void drawString(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawString(text, (int)x, (int)y, color);
    }

    public static void drawString(FontRenderer fontRenderer, String text, float x, float y){
        drawString(fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(String text, float x, float y, int color){
        drawString(ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(String text, float x, float y){
        drawString(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y){
        drawStringWithShadow(fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(String text, float x, float y, int color){
        drawStringWithShadow(ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(String text, float x, float y){
        drawStringWithShadow(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String text, float x, float y, int color){
        drawString(text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String text, float x, float y){
        drawString(text, x - fontRenderer.getStringWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(String text, float x, float y, int color){
        drawCenteredString(ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(String text, float x, float y){
        drawCenteredString(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color){
        drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, String text, float x, float y){
        drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(String text, float x, float y, int color){
        drawCenteredStringWithShadow(ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(String text, float x, float y){
        drawCenteredStringWithShadow(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawScreenBackground(float x, float y, float width, float height){
        bindTexture(SCREEN_BACKGROUND);
        // corners
        drawTexture(x, y, 4, 4, 0, 0, 4 / 9f, 4 / 9f);
        drawTexture(x + width - 4, y, 4, 4, 5 / 9f, 0, 4 / 9f, 4 / 9f);
        drawTexture(x + width - 4, y + height - 4, 4, 4, 5 / 9f, 5 / 9f, 4 / 9f, 4 / 9f);
        drawTexture(x, y + height - 4, 4, 4, 0, 5 / 9f, 4 / 9f, 4 / 9f);
        // edges
        drawTexture(x + 4, y, width - 8, 4, 4 / 9f, 0, 1 / 9f, 4 / 9f);
        drawTexture(x + 4, y + height - 4, width - 8, 4, 4 / 9f, 5 / 9f, 1 / 9f, 4 / 9f);
        drawTexture(x, y + 4, 4, height - 8, 0, 4 / 9f, 4 / 9f, 1 / 9f);
        drawTexture(x + width - 4, y + 4, 4, height - 8, 5 / 9f, 4 / 9f, 4 / 9f, 1 / 9f);
        // center
        drawTexture(x + 4, y + 4, width - 8, height - 8, 4 / 9f, 4 / 9f, 1 / 9f, 1 / 9f);
    }

    public static void drawButtonBackground(float x, float y, float width, float height, float yOffset){
        bindTexture(BUTTON_BACKGROUND);
        // corners
        drawTexture(x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        drawTexture(x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        drawTexture(x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        drawTexture(x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        drawTexture(x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        drawTexture(x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        drawTexture(x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    public static void drawTexture(float x, float y, float width, float height){
        drawTexture(x, y, width, height, 0, 0, 1, 1);
    }

    public static void drawTexture(float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        GlStateManager.color(1, 1, 1, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(tx, ty + theight).endVertex();
        buffer.pos(x + width, y + height, 0).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(x + width, y, 0).tex(tx + twidth, ty).endVertex();
        buffer.pos(x, y, 0).tex(tx, ty).endVertex();
        tessellator.draw();
    }

    public static void fillRect(float x, float y, float width, float height, int color){
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        fillRect(x, y, width, height, red, green, blue, alpha);
    }

    public static void fillRect(float x, float y, float width, float height, float red, float green, float blue, float alpha){
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + width, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void bindTexture(ResourceLocation location){
        ClientUtils.getTextureManager().bindTexture(location);
    }

    public static void drawTooltip(FontRenderer fontRenderer, List<ITextComponent> text, int x, int y){
        drawTooltipInternal(fontRenderer, text.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()), x, y);
    }

    public static void drawTooltip(FontRenderer fontRenderer, ITextComponent text, int x, int y){
        drawTooltip(fontRenderer, Collections.singletonList(text), x, y);
    }

    public static void drawTooltip(FontRenderer fontRenderer, String text, int x, int y){
        drawTooltip(fontRenderer, new TextComponentString(text), x, y);
    }

    public static void drawTooltip(List<ITextComponent> text, int x, int y){
        drawTooltip(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawTooltip(ITextComponent text, int x, int y){
        drawTooltip(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawTooltip(String text, int x, int y){
        drawTooltip(ClientUtils.getFontRenderer(), text, x, y);
    }

    /**
     * Copied from {@link GuiScreen#drawHoveringText(List, int, int, FontRenderer)}.
     */
    private static void drawTooltipInternal(FontRenderer fontRenderer, List<String> components, int x, int y){
        if(components.isEmpty())
            return;

        // Calculate scaled width and height
        int displayWidth = ClientUtils.getMinecraft().displayWidth;
        int displayHeight = ClientUtils.getMinecraft().displayHeight;
        int scaleFactor = 1;
        boolean isUnicode = ClientUtils.getMinecraft().isUnicode();
        int guiScale = ClientUtils.getMinecraft().gameSettings.guiScale;
        if(guiScale == 0)
            guiScale = 1000;
        while(scaleFactor < guiScale && displayWidth / (scaleFactor + 1) >= 320 && displayHeight / (scaleFactor + 1) >= 240){
            scaleFactor++;
        }
        if(isUnicode && scaleFactor % 2 != 0 && scaleFactor != 1)
            scaleFactor--;

        int screenWidth = MathHelper.ceil((double)displayWidth / scaleFactor);
        int screenHeight = MathHelper.ceil((double)displayHeight / scaleFactor);
        GuiUtils.drawHoveringText(components, x, y, screenWidth, screenHeight, -1, fontRenderer);
    }
}
