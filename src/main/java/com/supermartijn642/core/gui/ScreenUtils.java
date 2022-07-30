package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
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

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawString(matrixStack, fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawString(matrixStack, fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawString(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawString(matrixStack, text.getColoredString(), x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawString(matrixStack, text.getColoredString(), x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawStringWithShadow(matrixStack, fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawStringWithShadow(matrixStack, fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawStringWithShadow(matrixStack, text.getColoredString(), x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawStringWithShadow(matrixStack, text.getColoredString(), x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawCenteredString(matrixStack, fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawCenteredString(matrixStack, fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawCenteredString(matrixStack, text.getColoredString(), x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawCenteredString(matrixStack, text.getColoredString(), x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(matrixStack, fontRenderer, text.getColoredString(), x, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(matrixStack, fontRenderer, text.getColoredString(), x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(matrixStack, text.getColoredString(), x, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(matrixStack, text.getColoredString(), x, y);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        RenderSystem.enableAlphaTest();
        fontRenderer.drawInternal(text, x, y, color, matrixStack.last().pose(), false);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        drawString(matrixStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        RenderSystem.enableAlphaTest();
        fontRenderer.drawInternal(text, x, y, color, matrixStack.last().pose(), true);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        drawStringWithShadow(matrixStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        drawString(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        drawString(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        drawStringWithShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        drawStringWithShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, String text, float x, float y){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawScreenBackground(MatrixStack matrixStack, float x, float y, float width, float height){
        Minecraft.getInstance().textureManager.bind(SCREEN_BACKGROUND);
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
        Minecraft.getInstance().getTextureManager().bind(BUTTON_BACKGROUND);
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
        GlStateManager._color4f(1, 1, 1, 1);

        Matrix4f matrix = matrixStack.last().pose();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.vertex(matrix, x, y + height, 0).uv(tx, ty + theight).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).uv(tx + twidth, ty + theight).endVertex();
        buffer.vertex(matrix, x + width, y, 0).uv(tx + twidth, ty).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(tx, ty).endVertex();
        tessellator.end();
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

        Matrix4f matrix = matrixStack.last().pose();
        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
        tesselator.end();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void bindTexture(ResourceLocation location){
        Minecraft.getInstance().textureManager.bind(location);
    }

    public static void drawTooltip(MatrixStack poseStack, FontRenderer fontRenderer, List<ITextComponent> text, int x, int y){
        drawTooltipInternal(poseStack, fontRenderer, text.stream().map(ITextComponent::getColoredString).collect(Collectors.toList()), x, y);
    }

    public static void drawTooltip(MatrixStack poseStack, FontRenderer fontRenderer, ITextComponent text, int x, int y){
        drawTooltip(poseStack, fontRenderer, Collections.singletonList(text), x, y);
    }

    public static void drawTooltip(MatrixStack poseStack, FontRenderer fontRenderer, String text, int x, int y){
        drawTooltip(poseStack, fontRenderer, new StringTextComponent(text), x, y);
    }

    public static void drawTooltip(MatrixStack poseStack, List<ITextComponent> text, int x, int y){
        drawTooltip(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawTooltip(MatrixStack poseStack, ITextComponent text, int x, int y){
        drawTooltip(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawTooltip(MatrixStack poseStack, String text, int x, int y){
        drawTooltip(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    /**
     * Copied from {@link Screen#renderTooltip(List, int, int, FontRenderer)}.
     */
    private static void drawTooltipInternal(MatrixStack poseStack, FontRenderer fontRenderer, List<String> components, int x, int y){
        if(components.isEmpty())
            return;

        int screenWidth = ClientUtils.getMinecraft().getWindow().getGuiScaledWidth();
        int screenHeight = ClientUtils.getMinecraft().getWindow().getGuiScaledHeight();

        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, components, x, y, screenWidth, screenHeight, -1, fontRenderer);
        if(MinecraftForge.EVENT_BUS.post(event))
            return;

        x = event.getX();
        y = event.getY();
        screenWidth = event.getScreenWidth();
        screenHeight = event.getScreenHeight();
        int maxTextWidth = event.getMaxWidth();
        fontRenderer = event.getFontRenderer();

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int tooltipTextWidth = 0;

        for(String textLine : components){
            int textLineWidth = fontRenderer.width(textLine);
            if(textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = x + 12;
        if(tooltipX + tooltipTextWidth + 4 > screenWidth){
            tooltipX = x - 16 - tooltipTextWidth;
            if(tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if(x > screenWidth / 2)
                    tooltipTextWidth = x - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - x;
                needsWrap = true;
            }
        }

        if(maxTextWidth > 0 && tooltipTextWidth > maxTextWidth){
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if(needsWrap){
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<String>();
            for(int i = 0; i < components.size(); i++){
                String textLine = components.get(i);
                List<String> wrappedLine = fontRenderer.split(textLine, tooltipTextWidth);
                if(i == 0)
                    titleLinesCount = wrappedLine.size();

                for(String line : wrappedLine){
                    int lineWidth = fontRenderer.width(line);
                    if(lineWidth > wrappedTooltipWidth)
                        wrappedTooltipWidth = lineWidth;
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            components = wrappedTextLines;

            if(x > screenWidth / 2)
                tooltipX = x - 16 - tooltipTextWidth;
            else
                tooltipX = x + 12;
        }

        int tooltipY = y - 12;
        int tooltipHeight = 8;

        if(components.size() > 1){
            tooltipHeight += (components.size() - 1) * 10;
            if(components.size() > titleLinesCount)
                tooltipHeight += 2; // gap between title lines and next lines
        }

        if(tooltipY < 4)
            tooltipY = 4;
        else if(tooltipY + tooltipHeight + 4 > screenHeight)
            tooltipY = screenHeight - tooltipHeight - 4;

        final int zLevel = 300;
        RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, components, tooltipX, tooltipY, fontRenderer, -267386864, 1347420415, 1344798847);
        MinecraftForge.EVENT_BUS.post(colorEvent);
        int backgroundColor = colorEvent.getBackground();
        int borderColorStart = colorEvent.getBorderStart();
        int borderColorEnd = colorEvent.getBorderEnd();

        drawGradientRect(poseStack, zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
        drawGradientRect(poseStack, zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
        drawGradientRect(poseStack, zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(poseStack, zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(poseStack, zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        drawGradientRect(poseStack, zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(poseStack, zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(poseStack, zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
        drawGradientRect(poseStack, zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(ItemStack.EMPTY, components, tooltipX, tooltipY, fontRenderer, tooltipTextWidth, tooltipHeight));

        IRenderTypeBuffer.Impl renderType = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        MatrixStack textStack = new MatrixStack();
        textStack.translate(0.0D, 0.0D, (double)zLevel);
        Matrix4f textLocation = textStack.last().pose();

        int tooltipTop = tooltipY;

        for(int lineNumber = 0; lineNumber < components.size(); ++lineNumber){
            String line = components.get(lineNumber);
            if(line != null)
                fontRenderer.drawInBatch(line, (float)tooltipX, (float)tooltipY, -1, true, textLocation, renderType, false, 0, 15728880);

            if(lineNumber + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += 10;
        }

        renderType.endBatch();

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, components, tooltipX, tooltipTop, fontRenderer, tooltipTextWidth, tooltipHeight));

        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    }

    /**
     * Copied from {@link net.minecraftforge.fml.client.gui.GuiUtils#drawGradientRect(int, int, int, int, int, int, int)} in order to add the matrix stack as an argument.
     */
    private static void drawGradientRect(MatrixStack poseStack, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor){
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >> 8 & 255) / 255.0F;
        float startBlue = (float)(startColor & 255) / 255.0F;
        float endAlpha = (float)(endColor >> 24 & 255) / 255.0F;
        float endRed = (float)(endColor >> 16 & 255) / 255.0F;
        float endGreen = (float)(endColor >> 8 & 255) / 255.0F;
        float endBlue = (float)(endColor & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        Matrix4f matrix4f = poseStack.last().pose();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(matrix4f, right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(matrix4f, left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(matrix4f, left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.vertex(matrix4f, right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.end();

        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
}
