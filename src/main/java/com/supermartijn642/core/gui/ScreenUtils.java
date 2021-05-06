package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class ScreenUtils {

    private static final ResourceLocation BUTTON_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/buttons.png");
    private static final ResourceLocation SCREEN_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/background.png");

    public static final int DEFAULT_TEXT_COLOR = 4210752, ACTIVE_TEXT_COLOR = 14737632, INACTIVE_TEXT_COLOR = 7368816;

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.func_238422_b_(matrixStack, text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.func_238422_b_(matrixStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.func_238407_a_(matrixStack, text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.func_238407_a_(matrixStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.func_238422_b_(matrixStack, text, x - fontRenderer.getStringPropertyWidth(text) / 2f, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.func_238422_b_(matrixStack, text, x - fontRenderer.getStringPropertyWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.func_238407_a_(matrixStack, text, x - fontRenderer.getStringPropertyWidth(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.func_238407_a_(matrixStack, text, x - fontRenderer.getStringPropertyWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawString(matrixStack, text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.drawString(matrixStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawStringWithShadow(matrixStack, text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.drawStringWithShadow(matrixStack, text, x - fontRenderer.getStringWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawString(matrixStack, text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.drawString(matrixStack, text, x - fontRenderer.getStringWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawStringWithShadow(matrixStack, text, x - fontRenderer.getStringWidth(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.drawStringWithShadow(matrixStack, text, x - fontRenderer.getStringWidth(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, String text, float x, float y){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawScreenBackground(MatrixStack matrixStack, float x, float y, float width, float height){
        Minecraft.getInstance().textureManager.bindTexture(SCREEN_BACKGROUND);
        // corners
        drawTexture(matrixStack, x, y, 4, 4, 0, 0, 4 / 9f, 4 / 9f);
        drawTexture(matrixStack, x + width - 4, y, 4, 4, 5 / 9f, 0, 4 / 9f, 4 / 9f);
        drawTexture(matrixStack, x + width - 4, y + height - 4, 4, 4, 5 / 9f, 5 / 9f, 4 / 9f, 4 / 9f);
        drawTexture(matrixStack, x, y + height - 4, 4, 4, 0, 5 / 9f, 4 / 9f, 4 / 9f);
        // edges
        drawTexture(matrixStack, x + 4, y, width - 8, 4, 4 / 9f, 0, 1 / 9f, 4 / 9f);
        drawTexture(matrixStack, x + 4, y + height - 4, width - 8, 4, 4 / 9f, 5 / 9f, 1 / 9f, 4 / 9f);
        drawTexture(matrixStack, x, y + 4, 4, height - 8, 0, 4 / 9f, 4 / 9f, 1 / 9f);
        drawTexture(matrixStack, x + width - 4, y + 4, 4, height - 8, 5 / 9f, 4 / 9f, 4 / 9f, 1 / 9f);
        // center
        drawTexture(matrixStack, x + 4, y + 4, width - 8, height - 8, 4 / 9f, 4 / 9f, 1 / 9f, 1 / 9f);
    }

    public static void drawButtonBackground(MatrixStack matrixStack, float x, float y, float width, float height, float yOffset){
        Minecraft.getInstance().getTextureManager().bindTexture(BUTTON_BACKGROUND);
        // corners
        drawTexture(matrixStack, x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(matrixStack, x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(matrixStack, x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        drawTexture(matrixStack, x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        drawTexture(matrixStack, x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        drawTexture(matrixStack, x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        drawTexture(matrixStack, x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        drawTexture(matrixStack, x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        drawTexture(matrixStack, x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    public static void drawTexture(MatrixStack matrixStack, float x, float y, float width, float height){
        drawTexture(matrixStack, x, y, width, height, 0, 0, 1, 1);
    }

    public static void drawTexture(MatrixStack matrixStack, float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        GlStateManager.color4f(1, 1, 1, 1);

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(matrix, x, y + height, 0).tex(tx, ty + theight).endVertex();
        buffer.pos(matrix, x + width, y + height, 0).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(matrix, x + width, y, 0).tex(tx + twidth, ty).endVertex();
        buffer.pos(matrix, x, y, 0).tex(tx, ty).endVertex();
        tessellator.draw();
    }

    public static void fillRect(MatrixStack matrixStack, float x, float y, float width, float height, int color){
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        fillRect(matrixStack, x, y, width, height, red, green, blue, alpha);
    }

    public static void fillRect(MatrixStack matrixStack, float x, float y, float width, float height, float red, float green, float blue, float alpha){
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void bindTexture(ResourceLocation location){
        Minecraft.getInstance().textureManager.bindTexture(location);
    }
}
