package com.supermartijn642.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class ScreenUtils {

    private static final ResourceLocation BUTTON_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/buttons.png");
    private static final ResourceLocation SCREEN_BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/background.png");

    public static final int DEFAULT_TEXT_COLOR = 4210752, ACTIVE_TEXT_COLOR = 14737632, INACTIVE_TEXT_COLOR = 7368816;

    public static void drawString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        fontRenderer.draw(poseStack, text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        fontRenderer.draw(poseStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(PoseStack poseStack, Component text, float x, float y, int color){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, Component text, float x, float y){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        fontRenderer.drawShadow(poseStack, text, x, y, color);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        fontRenderer.drawShadow(poseStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Component text, float x, float y, int color){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Component text, float x, float y){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        fontRenderer.draw(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        fontRenderer.draw(poseStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(PoseStack poseStack, Component text, float x, float y, int color){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Component text, float x, float y){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        fontRenderer.drawShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        fontRenderer.drawShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Component text, float x, float y, int color){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Component text, float x, float y){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawString(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        fontRenderer.draw(poseStack, text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        fontRenderer.draw(poseStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(PoseStack poseStack, String text, float x, float y, int color){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, String text, float x, float y){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        fontRenderer.drawShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(PoseStack poseStack, String text, float x, float y, int color){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(PoseStack poseStack, String text, float x, float y){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        fontRenderer.draw(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        fontRenderer.draw(poseStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(PoseStack poseStack, String text, float x, float y, int color){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, String text, float x, float y){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        fontRenderer.drawShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, String text, float x, float y, int color){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, String text, float x, float y){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawScreenBackground(PoseStack poseStack, float x, float y, float width, float height){
        bindTexture(SCREEN_BACKGROUND);

        // corners
        drawTexture(poseStack, x, y, 4, 4, 0, 0, 4 / 9f, 4 / 9f);
        drawTexture(poseStack, x + width - 4, y, 4, 4, 5 / 9f, 0, 4 / 9f, 4 / 9f);
        drawTexture(poseStack, x + width - 4, y + height - 4, 4, 4, 5 / 9f, 5 / 9f, 4 / 9f, 4 / 9f);
        drawTexture(poseStack, x, y + height - 4, 4, 4, 0, 5 / 9f, 4 / 9f, 4 / 9f);
        // edges
        drawTexture(poseStack, x + 4, y, width - 8, 4, 4 / 9f, 0, 1 / 9f, 4 / 9f);
        drawTexture(poseStack, x + 4, y + height - 4, width - 8, 4, 4 / 9f, 5 / 9f, 1 / 9f, 4 / 9f);
        drawTexture(poseStack, x, y + 4, 4, height - 8, 0, 4 / 9f, 4 / 9f, 1 / 9f);
        drawTexture(poseStack, x + width - 4, y + 4, 4, height - 8, 5 / 9f, 4 / 9f, 4 / 9f, 1 / 9f);
        // center
        drawTexture(poseStack, x + 4, y + 4, width - 8, height - 8, 4 / 9f, 4 / 9f, 1 / 9f, 1 / 9f);
    }

    public static void drawButtonBackground(PoseStack poseStack, float x, float y, float width, float height, float yOffset){
        bindTexture(BUTTON_BACKGROUND);
        // corners
        drawTexture(poseStack, x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(poseStack, x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(poseStack, x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        drawTexture(poseStack, x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        drawTexture(poseStack, x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        drawTexture(poseStack, x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        drawTexture(poseStack, x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        drawTexture(poseStack, x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        drawTexture(poseStack, x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    public static void drawTexture(PoseStack poseStack, float x, float y, float width, float height){
        drawTexture(poseStack, x, y, width, height, 0, 0, 1, 1);
    }

    public static void drawTexture(PoseStack poseStack, float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // TODO check if this works

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x, y + height, 0).uv(tx, ty + theight).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).uv(tx + twidth, ty + theight).endVertex();
        buffer.vertex(matrix, x + width, y, 0).uv(tx + twidth, ty).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(tx, ty).endVertex();
        tessellator.end();
    }

    public static void fillRect(PoseStack poseStack, float x, float y, float width, float height, int color){
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        fillRect(poseStack, x, y, width, height, red, green, blue, alpha);
    }

    public static void fillRect(PoseStack poseStack, float x, float y, float width, float height, float red, float green, float blue, float alpha){
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void bindTexture(ResourceLocation location){
        RenderSystem.setShaderTexture(0, location);
    }
}
