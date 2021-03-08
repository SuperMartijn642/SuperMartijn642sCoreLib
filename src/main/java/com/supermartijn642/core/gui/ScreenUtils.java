package com.supermartijn642.core.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class ScreenUtils {

    private static final ResourceLocation BUTTON_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/buttons.png");
    private static final ResourceLocation SCREEN_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/background.png");

    public static final int ACTIVE_TEXT_COLOR = 14737632, INACTIVE_TEXT_COLOR = 7368816;

    public static void drawString(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.drawString(text.getFormattedText(), (int)x, (int)y, color);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.drawStringWithShadow(text.getFormattedText(), x, y, color);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        String s = text.getFormattedText();
        fontRenderer.drawString(s, (int)(x - fontRenderer.getStringWidth(s) / 2f), (int)y, color);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        String s = text.getFormattedText();
        fontRenderer.drawStringWithShadow(s, x - fontRenderer.getStringWidth(s) / 2f, y, color);
    }

    public static void drawString(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawString(text, (int)x, (int)y, color);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawString(text, (int)(x - fontRenderer.getStringWidth(text) / 2f), (int)y, color);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
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
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
    }
}
