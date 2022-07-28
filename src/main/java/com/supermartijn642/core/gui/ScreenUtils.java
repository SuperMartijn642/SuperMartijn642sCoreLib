package com.supermartijn642.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;

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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
        tesselator.end();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void bindTexture(ResourceLocation location){
        RenderSystem.setShaderTexture(0, location);
    }

    public static void drawTooltip(PoseStack poseStack, Font fontRenderer, List<Component> text, int x, int y){
        drawTooltipInternal(poseStack, fontRenderer, text.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList()), x, y);
    }

    public static void drawTooltip(PoseStack poseStack, Font fontRenderer, Component text, int x, int y){
        drawTooltip(poseStack, fontRenderer, Collections.singletonList(text), x, y);
    }

    public static void drawTooltip(PoseStack poseStack, Font fontRenderer, String text, int x, int y){
        drawTooltip(poseStack, fontRenderer, new TextComponent(text), x, y);
    }

    public static void drawTooltip(PoseStack poseStack, List<Component> text, int x, int y){
        drawTooltip(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawTooltip(PoseStack poseStack, Component text, int x, int y){
        drawTooltip(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawTooltip(PoseStack poseStack, String text, int x, int y){
        drawTooltip(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    /**
     * Copied from {@link Screen#renderTooltipInternal(PoseStack, List, int, int)}.
     */
    private static void drawTooltipInternal(PoseStack poseStack, Font fontRenderer, List<ClientTooltipComponent> components, int x, int y){
        if(components.isEmpty())
            return;

        int windowWidth = ClientUtils.getMinecraft().getWindow().getGuiScaledWidth();
        int windowHeight = ClientUtils.getMinecraft().getWindow().getGuiScaledHeight();

        RenderTooltipEvent.Pre preEvent = ForgeHooksClient.preTooltipEvent(ItemStack.EMPTY, poseStack, x, y, windowWidth, windowHeight, components, fontRenderer, ClientUtils.getFontRenderer());
        if(preEvent.isCanceled())
            return;

        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;

        for(ClientTooltipComponent component : components){
            int componentWidth = component.getWidth(preEvent.getFontRenderer());
            if(componentWidth > tooltipWidth)
                tooltipWidth = componentWidth;

            tooltipHeight += component.getHeight();
        }

        int tooltipX = preEvent.getX() + 12;
        int tooltipY = preEvent.getY() - 12;
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
        ItemRenderer itemRenderer = ClientUtils.getItemRenderer();
        float oldBlitOffset = itemRenderer.blitOffset;
        itemRenderer.blitOffset = 400.0F;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
        RenderTooltipEvent.Color colorEvent = ForgeHooksClient.colorTooltipEvent(ItemStack.EMPTY, poseStack, tooltipX, tooltipY, preEvent.getFontRenderer(), components);
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundStart());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, 400, colorEvent.getBackgroundEnd(), colorEvent.getBackgroundEnd());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderStart());
        GuiComponent.fillGradient(matrix4f, bufferbuilder, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, colorEvent.getBorderEnd(), colorEvent.getBorderEnd());
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0D, 0.0D, 400.0D);

        int offsetY = tooltipY;

        for(int index = 0; index < components.size(); ++index){
            ClientTooltipComponent component = components.get(index);
            component.renderText(preEvent.getFontRenderer(), tooltipX, offsetY, matrix4f, bufferSource);
            offsetY += component.getHeight() + (index == 0 ? 2 : 0);
        }

        bufferSource.endBatch();
        poseStack.popPose();
        offsetY = tooltipY;

        for(int index = 0; index < components.size(); ++index){
            ClientTooltipComponent component = components.get(index);
            component.renderImage(preEvent.getFontRenderer(), tooltipX, offsetY, poseStack, itemRenderer, 400, ClientUtils.getTextureManager());
            offsetY += component.getHeight() + (index == 0 ? 2 : 0);
        }

        itemRenderer.blitOffset = oldBlitOffset;
    }
}
