package com.supermartijn642.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
        fontRenderer.draw(matrixStack, text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.draw(matrixStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.drawShadow(matrixStack, text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.drawShadow(matrixStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.draw(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.draw(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y, int color){
        fontRenderer.drawShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, ITextComponent text, float x, float y){
        fontRenderer.drawShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y, int color){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, ITextComponent text, float x, float y){
        drawCenteredStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.draw(matrixStack, text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.draw(matrixStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y){
        drawString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.drawShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y){
        drawStringWithShadow(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.draw(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.draw(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y){
        drawCenteredString(matrixStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y, int color){
        fontRenderer.drawShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer fontRenderer, String text, float x, float y){
        fontRenderer.drawShadow(matrixStack, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
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
        drawTooltipInternal(poseStack, fontRenderer, text.stream().map(ITextComponent::getVisualOrderText).collect(Collectors.toList()), x, y);
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
     * Copied from {@link Screen#renderToolTip(MatrixStack, List, int, int, FontRenderer)}.
     */
    private static void drawTooltipInternal(MatrixStack poseStack, FontRenderer fontRenderer, List<? extends IReorderingProcessor> components, int x, int y){
        if(components.isEmpty())
            return;

        int windowWidth = ClientUtils.getMinecraft().getWindow().getGuiScaledWidth();
        int windowHeight = ClientUtils.getMinecraft().getWindow().getGuiScaledHeight();

        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;

        for(IReorderingProcessor component : components){
            int componentWidth = fontRenderer.width(component);
            if(componentWidth > tooltipWidth)
                tooltipWidth = componentWidth;

            tooltipHeight += 10;
        }

        int tooltipX = x + 12;
        int tooltipY = y - 12;
        if(tooltipX + tooltipWidth > windowWidth){
            tooltipX -= 28 + tooltipWidth;
        }

        if(tooltipY + tooltipHeight + 6 > windowHeight){
            tooltipY = windowHeight - tooltipHeight - 6;
        }

        if(y - tooltipHeight - 8 < 0){
            tooltipY = y + 8;
        }

        poseStack.pushPose();

        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, 400, -267386864, -267386864);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, 400, -267386864, -267386864);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, 400, 1347420415, 1347420415);
        AbstractGui.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        IRenderTypeBuffer.Impl bufferSource = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        poseStack.translate(0.0D, 0.0D, 400.0D);

        for(int index = 0; index < components.size(); ++index){
            IReorderingProcessor component = components.get(index);
            if(component != null)
                fontRenderer.drawInBatch(component, tooltipX, tooltipY, -1, true, matrix4f, bufferSource, false, 0, 15728880);
            tooltipY += index == 0 ? 12 : 10;
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }
}
