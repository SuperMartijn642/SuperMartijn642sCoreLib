package com.supermartijn642.core.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
        drawString(fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawString(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawString(fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawString(ITextComponent text, float x, float y, int color){
        drawString(text.getColoredString(), x, y, color);
    }

    public static void drawString(ITextComponent text, float x, float y){
        drawString(text.getColoredString(), x, y);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawStringWithShadow(fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawStringWithShadow(fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawStringWithShadow(ITextComponent text, float x, float y, int color){
        drawStringWithShadow(text.getColoredString(), x, y, color);
    }

    public static void drawStringWithShadow(ITextComponent text, float x, float y){
        drawStringWithShadow(text.getColoredString(), x, y);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawCenteredString(fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawCenteredString(fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawCenteredString(ITextComponent text, float x, float y, int color){
        drawCenteredString(text.getColoredString(), x, y, color);
    }

    public static void drawCenteredString(ITextComponent text, float x, float y){
        drawCenteredString(text.getColoredString(), x, y);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawCenteredStringWithShadow(ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(text.getColoredString(), x, y, color);
    }

    public static void drawCenteredStringWithShadow(ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(text.getColoredString(), x, y);
    }

    public static void drawString(FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.draw(text, x, y, color);
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
        fontRenderer.drawShadow(text, x, y, color);
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
        drawString(text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String text, float x, float y){
        drawString(text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(String text, float x, float y, int color){
        drawCenteredString(ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(String text, float x, float y){
        drawCenteredString(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color){
        drawStringWithShadow(text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(FontRenderer fontRenderer, String text, float x, float y){
        drawStringWithShadow(text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(String text, float x, float y, int color){
        drawCenteredStringWithShadow(ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(String text, float x, float y){
        drawCenteredStringWithShadow(ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawScreenBackground(float x, float y, float width, float height){
        Minecraft.getInstance().textureManager.bind(SCREEN_BACKGROUND);
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
        Minecraft.getInstance().getTextureManager().bind(BUTTON_BACKGROUND);
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
        GlStateManager.color4f(1, 1, 1, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex(x, y + height, 0).uv(tx, ty + theight).endVertex();
        buffer.vertex(x + width, y + height, 0).uv(tx + twidth, ty + theight).endVertex();
        buffer.vertex(x + width, y, 0).uv(tx + twidth, ty).endVertex();
        buffer.vertex(x, y, 0).uv(tx, ty).endVertex();
        tessellator.end();
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
        GlStateManager.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(x, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(x + width, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(x, y, 0).color(red, green, blue, alpha).endVertex();
        tessellator.end();

        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void bindTexture(ResourceLocation location){
        Minecraft.getInstance().textureManager.bind(location);
    }

    public static void drawTooltip(FontRenderer fontRenderer, List<ITextComponent> text, int x, int y){
        drawTooltipInternal(fontRenderer, text.stream().map(ITextComponent::getColoredString).collect(Collectors.toList()), x, y);
    }

    public static void drawTooltip(FontRenderer fontRenderer, ITextComponent text, int x, int y){
        drawTooltip(fontRenderer, Collections.singletonList(text), x, y);
    }

    public static void drawTooltip(FontRenderer fontRenderer, String text, int x, int y){
        drawTooltip(fontRenderer, new StringTextComponent(text), x, y);
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
     * Copied from {@link Screen#renderTooltip(List, int, int, FontRenderer)}.
     */
    private static void drawTooltipInternal(FontRenderer fontRenderer, List<String> components, int x, int y){
        if(components.isEmpty())
            return;

        int screenWidth = ClientUtils.getMinecraft().window.getGuiScaledWidth();
        int screenHeight = ClientUtils.getMinecraft().window.getGuiScaledHeight();
        GuiUtils.drawHoveringText(components, x, y, screenWidth, screenHeight, -1, fontRenderer);
    }
}
