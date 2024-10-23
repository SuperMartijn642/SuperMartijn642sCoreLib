package com.supermartijn642.core.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 1/20/2021 by SuperMartijn642
 */
public class ScreenUtils {

    private static final ResourceLocation BUTTON_BACKGROUND = ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "textures/gui/buttons.png");
    private static final ResourceLocation SCREEN_BACKGROUND = ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "textures/gui/background.png");
    private static final GuiGraphics GUI_GRAPHICS = new GuiGraphics(ClientUtils.getMinecraft(), null);

    public static final int DEFAULT_TEXT_COLOR = 4210752, ACTIVE_TEXT_COLOR = 14737632, INACTIVE_TEXT_COLOR = 7368816;

    public static void drawString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        fontRenderer.drawInBatch(text, x, y, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void drawString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        drawString(poseStack, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(PoseStack poseStack, Component text, float x, float y, int color){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, Component text, float x, float y){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        fontRenderer.drawInBatch(text, x, y, color, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        drawStringWithShadow(poseStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Component text, float x, float y, int color){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Component text, float x, float y){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        drawString(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        drawCenteredString(poseStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(PoseStack poseStack, Component text, float x, float y, int color){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Component text, float x, float y){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y, int color){
        drawStringWithShadow(poseStack, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, Component text, float x, float y){
        drawCenteredStringWithShadow(poseStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Component text, float x, float y, int color){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Component text, float x, float y){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawString(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        fontRenderer.drawInBatch(text, x, y, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void drawString(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        drawString(poseStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawString(PoseStack poseStack, String text, float x, float y, int color){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawString(PoseStack poseStack, String text, float x, float y){
        drawString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        fontRenderer.drawInBatch(text, x, y, color, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void drawStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        drawStringWithShadow(poseStack, fontRenderer, text, x - fontRenderer.width(text) / 2f, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawStringWithShadow(PoseStack poseStack, String text, float x, float y, int color){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawStringWithShadow(PoseStack poseStack, String text, float x, float y){
        drawStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        drawString(poseStack, fontRenderer, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        drawCenteredString(poseStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredString(PoseStack poseStack, String text, float x, float y, int color){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredString(PoseStack poseStack, String text, float x, float y){
        drawCenteredString(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y, int color){
        drawStringWithShadow(poseStack, fontRenderer, text, x - fontRenderer.width(text) / 2f, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, Font fontRenderer, String text, float x, float y){
        drawCenteredStringWithShadow(poseStack, fontRenderer, text, x, y, DEFAULT_TEXT_COLOR);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, String text, float x, float y, int color){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y, color);
    }

    public static void drawCenteredStringWithShadow(PoseStack poseStack, String text, float x, float y){
        drawCenteredStringWithShadow(poseStack, ClientUtils.getFontRenderer(), text, x, y);
    }

    public static void drawScreenBackground(PoseStack poseStack, float x, float y, float width, float height){
        // corners
        drawTexture(SCREEN_BACKGROUND, poseStack, x, y, 4, 4, 0, 0, 4 / 9f, 4 / 9f);
        drawTexture(SCREEN_BACKGROUND, poseStack, x + width - 4, y, 4, 4, 5 / 9f, 0, 4 / 9f, 4 / 9f);
        drawTexture(SCREEN_BACKGROUND, poseStack, x + width - 4, y + height - 4, 4, 4, 5 / 9f, 5 / 9f, 4 / 9f, 4 / 9f);
        drawTexture(SCREEN_BACKGROUND, poseStack, x, y + height - 4, 4, 4, 0, 5 / 9f, 4 / 9f, 4 / 9f);
        // edges
        drawTexture(SCREEN_BACKGROUND, poseStack, x + 4, y, width - 8, 4, 4 / 9f, 0, 1 / 9f, 4 / 9f);
        drawTexture(SCREEN_BACKGROUND, poseStack, x + 4, y + height - 4, width - 8, 4, 4 / 9f, 5 / 9f, 1 / 9f, 4 / 9f);
        drawTexture(SCREEN_BACKGROUND, poseStack, x, y + 4, 4, height - 8, 0, 4 / 9f, 4 / 9f, 1 / 9f);
        drawTexture(SCREEN_BACKGROUND, poseStack, x + width - 4, y + 4, 4, height - 8, 5 / 9f, 4 / 9f, 4 / 9f, 1 / 9f);
        // center
        drawTexture(SCREEN_BACKGROUND, poseStack, x + 4, y + 4, width - 8, height - 8, 4 / 9f, 4 / 9f, 1 / 9f, 1 / 9f);
    }

    public static void drawButtonBackground(PoseStack poseStack, float x, float y, float width, float height, float yOffset){
        // corners
        drawTexture(BUTTON_BACKGROUND, poseStack, x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(BUTTON_BACKGROUND, poseStack, x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        drawTexture(BUTTON_BACKGROUND, poseStack, x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        drawTexture(BUTTON_BACKGROUND, poseStack, x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        drawTexture(BUTTON_BACKGROUND, poseStack, x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        drawTexture(BUTTON_BACKGROUND, poseStack, x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        drawTexture(BUTTON_BACKGROUND, poseStack, x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        drawTexture(BUTTON_BACKGROUND, poseStack, x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        drawTexture(BUTTON_BACKGROUND, poseStack, x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    public static void drawTexture(ResourceLocation texture, PoseStack poseStack, float x, float y, float width, float height){
        drawTexture(texture, poseStack, x, y, width, height, 0, 0, 1, 1);
    }

    public static void drawTexture(ResourceLocation texture, PoseStack poseStack, float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.guiTextured(texture));
        buffer.addVertex(matrix, x, y + height, 0).setUv(tx, ty + theight);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(tx + twidth, ty + theight);
        buffer.addVertex(matrix, x + width, y, 0).setUv(tx + twidth, ty);
        buffer.addVertex(matrix, x, y, 0).setUv(tx, ty);
    }

    public static void fillRect(PoseStack poseStack, float x, float y, float width, float height, int color){
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        fillRect(poseStack, x, y, width, height, red, green, blue, alpha);
    }

    public static void fillRect(PoseStack poseStack, float x, float y, float width, float height, float red, float green, float blue, float alpha){
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
        buffer.addVertex(matrix, x, y + height, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x + width, y + height, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x + width, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x, y, 0).setColor(red, green, blue, alpha);
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
        drawTooltip(poseStack, fontRenderer, Component.literal(text), x, y);
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
     * Copied from {@link GuiGraphics#renderTooltipInternal(Font, List, int, int, ClientTooltipPositioner, ResourceLocation)}.
     */
    private static void drawTooltipInternal(PoseStack poseStack, Font fontRenderer, List<ClientTooltipComponent> components, int x, int y){
        if(components.isEmpty())
            return;

        GUI_GRAPHICS.minecraft = ClientUtils.getMinecraft();
        GUI_GRAPHICS.pose = poseStack;
        GUI_GRAPHICS.bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
        GUI_GRAPHICS.renderTooltipInternal(fontRenderer, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
        GUI_GRAPHICS.bufferSource.endBatch();
    }

    /**
     * Copied from {@link GuiGraphics#renderItem(LivingEntity, Level, ItemStack, int, int, int, int)}
     */
    @SuppressWarnings("JavadocReference")
    public static void drawItem(PoseStack poseStack, ItemStack stack, @Nullable Level level, int x, int y){
        if(stack.isEmpty())
            return;

        poseStack.pushPose();
        poseStack.translate(x + 8, y + 8, 150);
        poseStack.mulPose(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
        poseStack.scale(16, 16, 16);
        try{
            BakedModel model = ClientUtils.getItemRenderer().getModel(stack, level, null, 0);
            boolean useFlatLighting = !model.usesBlockLight();
            if(useFlatLighting)
                Lighting.setupForFlatItems();
            RenderSystem.disableDepthTest();

            MultiBufferSource.BufferSource bufferSource = ClientUtils.getMinecraft().gameRenderer.renderBuffers.bufferSource();
            ClientUtils.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
            bufferSource.endBatch();

            RenderSystem.enableDepthTest();
            if(useFlatLighting)
                Lighting.setupFor3DItems();
        }catch(Throwable throwable){
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
            crashReportCategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
            crashReportCategory.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
            crashReportCategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
            throw new ReportedException(crashReport);
        }
        poseStack.popPose();
    }
}
