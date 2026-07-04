package com.supermartijn642.core.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.extensions.GuiGraphicsExtractorExtension;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created 28/06/2025 by SuperMartijn642
 */
public final class GuiGraphicsHelper {

    public static GuiGraphicsHelper of(GuiGraphicsExtractor guiGraphics){
        GuiGraphicsHelper helper = ((GuiGraphicsExtractorExtension)guiGraphics).supermartijn642corelibGetHelper();
        if(helper == null){
            helper = new GuiGraphicsHelper(guiGraphics);
            ((GuiGraphicsExtractorExtension)guiGraphics).supermartijn642corelibSetHelper(helper);
        }
        return helper;
    }

    public static final Identifier SCREEN_BACKGROUND_SPRITE = Identifier.fromNamespaceAndPath("supermartijn642corelib", "gui/background");
    public static final Identifier BUTTON_DEFAULT_SPRITE = Identifier.fromNamespaceAndPath("supermartijn642corelib", "gui/button_default");
    public static final Identifier BUTTON_HIGHLIGHTED_SPRITE = Identifier.fromNamespaceAndPath("supermartijn642corelib", "gui/button_highlighted");
    public static final Identifier BUTTON_DISABLED_SPRITE = Identifier.fromNamespaceAndPath("supermartijn642corelib", "gui/button_disabled");
    public static final Identifier SLOT_SPRITE = Identifier.fromNamespaceAndPath("supermartijn642corelib", "gui/slot");

    private final GuiGraphicsExtractor guiGraphics;
    private TextProperties textProperties;
    private TextureProperties textureProperties;
    private RectangleProperties rectangleProperties;
    private TooltipContent tooltipContent;
    private TooltipProperties tooltipProperties;
    private ItemProperties itemProperties;

    private GuiGraphicsHelper(GuiGraphicsExtractor guiGraphics){
        this.guiGraphics = guiGraphics;
    }

    public Matrix3x2fStack poseStack(){
        return this.guiGraphics.pose();
    }

    public void pushScissor(float x, float y, int width, int height){
        this.guiGraphics.enableScissor((int)x, (int)y, (int)(x + width), (int)(y + height));
    }

    public void popScissor(){
        this.guiGraphics.disableScissor();
    }

    public void nextStratum(){
        this.guiGraphics.nextStratum();
    }

    public boolean isPointInScissor(float x, float y){
        return this.guiGraphics.containsPointInScissor((int)x, (int)y);
    }

    /**
     * @see CursorTypes
     */
    public void requestCursor(CursorType cursorType){
        this.guiGraphics.requestCursor(cursorType);
    }

    public void submitText(FormattedText text, float x, float y, Consumer<TextProperties> properties){
        // Resolve the properties
        if(this.textProperties == null)
            this.textProperties = new TextProperties();
        else
            this.textProperties.clear();
        if(properties != null)
            properties.accept(this.textProperties);

        // Handle wrapped text
        Font font = this.textProperties.font == null ? ClientUtils.getFontRenderer() : this.textProperties.font;
        if(this.textProperties.wrapWidth != null){
            // Split lines
            List<FormattedCharSequence> lines = font.split(text, this.textProperties.wrapWidth);
            // Centering
            if(this.textProperties.centerHorizontally){
                int widestLine = 0;
                for(FormattedCharSequence line : lines){
                    int width = font.width(line);
                    if(width > widestLine)
                        widestLine = width;
                }
                x -= widestLine / 2f;
            }
            if(this.textProperties.centerVertically)
                y -= lines.size() * font.lineHeight / 2f;
            // Submit the lines
            Matrix3x2f pose = new Matrix3x2f(this.guiGraphics.pose());
            ScreenRectangle scissor = this.guiGraphics.scissorStack.peek();
            for(FormattedCharSequence line : lines){
                this.guiGraphics.guiRenderState.addText(new GuiTextRenderState(
                    font,
                    line,
                    pose,
                    (int)x,
                    (int)y,
                    this.textProperties.color,
                    this.textProperties.backgroundColor,
                    this.textProperties.shadow,
                    true,
                    scissor
                ));
            }
            return;
        }

        // Handle cutoff
        if(this.textProperties.cutoffWidth != null){
            int cutoff = this.textProperties.cutoffWidth;
            if(this.textProperties.cutoffPostfix != null)
                cutoff -= font.width(this.textProperties.cutoffPostfix);
            if(cutoff >= 0){
                text = font.substrByWidth(text, cutoff);
                if(this.textProperties.cutoffPostfix != null)
                    text = FormattedText.composite(text, this.textProperties.cutoffPostfix);
            }else
                text = font.substrByWidth(this.textProperties.cutoffPostfix, this.textProperties.cutoffWidth);
        }
        // Handle centering
        if(this.textProperties.centerHorizontally)
            x -= font.width(text) / 2f;
        if(this.textProperties.centerVertically)
            y -= font.lineHeight / 2f;
        // Submit the text
        this.guiGraphics.guiRenderState.addText(new GuiTextRenderState(
            font,
            Language.getInstance().getVisualOrder(text),
            new Matrix3x2f(this.guiGraphics.pose()),
            (int)x,
            (int)y,
            this.textProperties.color,
            this.textProperties.backgroundColor,
            this.textProperties.shadow,
            true,
            this.guiGraphics.scissorStack.peek()
        ));
    }

    public void submitText(FormattedText text, float x, float y){
        this.submitText(text, x, y, null);
    }

    public void submitText(String text, float x, float y, Consumer<TextProperties> properties){
        this.submitText(Component.literal(text), x, y, properties);
    }

    public void submitText(String text, float x, float y){
        this.submitText(Component.literal(text), x, y, null);
    }

    public void submitTexture(GpuTextureView texture, GpuSampler sampler, float x, float y, float width, float height, Consumer<TextureProperties> properties){
        // Resolve the properties
        if(this.textureProperties == null)
            this.textureProperties = new TextureProperties();
        else
            this.textureProperties.clear();
        if(properties != null)
            properties.accept(this.textureProperties);

        // Handle centering
        if(this.textureProperties.centerHorizontally)
            x -= width / 2f;
        if(this.textureProperties.centerVertically)
            y -= height / 2f;

        // Submit the texture
        this.guiGraphics.innerBlit(
            this.textureProperties.renderPipeline,
            texture, sampler,
            (int)x, (int)y, (int)(x + width), (int)(y + height),
            this.textureProperties.u, this.textureProperties.u + this.textureProperties.w, this.textureProperties.v, this.textureProperties.v + this.textureProperties.h,
            this.textureProperties.color
        );
    }

    public void submitTexture(GpuTextureView texture, GpuSampler sampler, float x, float y, float width, float height){
        this.submitTexture(texture, sampler, x, y, width, height, null);
    }

    public void submitTexture(Identifier texture, float x, float y, float width, float height, Consumer<TextureProperties> properties){
        AbstractTexture t = this.guiGraphics.minecraft.getTextureManager().getTexture(texture);
        this.submitTexture(t.getTextureView(), t.getSampler(), x, y, width, height, properties);
    }

    public void submitTexture(Identifier texture, float x, float y, float width, float height){
        this.submitTexture(texture, x, y, width, height, null);
    }

    public void submitSprite(TextureAtlasSprite sprite, float x, float y, float width, float height, Consumer<TextureProperties> properties){
        GuiSpriteScaling scaling = sprite.contents().getAdditionalMetadata(GuiMetadataSection.TYPE).orElse(GuiMetadataSection.DEFAULT).scaling();

        // Handle stretch scaling
        if(scaling instanceof GuiSpriteScaling.Stretch || !sprite.atlasLocation().equals(this.guiGraphics.guiSprites.location())){
            if(properties == null)
                properties = p -> {};
            this.submitTexture(
                sprite.atlasLocation(),
                x, y, width, height,
                properties.andThen(p -> {
                    float u = sprite.getU(p.u);
                    float v = sprite.getV(p.v);
                    p.uv(u, v, sprite.getU(p.u + p.w) - u, sprite.getV(p.v + p.h) - v);
                })
            );
            return;
        }

        // Could be some mod-added scaling, so call the original method as it likely gets intercepted there
        // Resolve properties
        if(this.textureProperties == null)
            this.textureProperties = new TextureProperties();
        else
            this.textureProperties.clear();
        if(properties != null)
            properties.accept(this.textureProperties);
        // Handle centering
        if(this.textureProperties.centerHorizontally)
            x -= width / 2f;
        if(this.textureProperties.centerVertically)
            y -= height / 2f;
        // Call original method
        this.guiGraphics.blitSprite(
            this.textureProperties.renderPipeline,
            sprite.contents().name(),
            (int)x, (int)y, (int)width, (int)height,
            this.textureProperties.color
        );
    }

    public void submitSprite(TextureAtlasSprite sprite, float x, float y, float width, float height){
        this.submitSprite(sprite, x, y, width, height, null);
    }

    public void submitSprite(Identifier sprite, float x, float y, float width, float height, Consumer<TextureProperties> properties){
        this.submitSprite(this.guiGraphics.guiSprites.getSprite(sprite), x, y, width, height, properties);
    }

    public void submitSprite(Identifier sprite, float x, float y, float width, float height){
        this.submitSprite(sprite, x, y, width, height, null);
    }

    public void submitDefaultScreenBackground(float x, float y, float width, float height){
        this.submitSprite(SCREEN_BACKGROUND_SPRITE, x, y, width, height, null);
    }

    public void submitDefaultButton(ButtonState state, float x, float y, float width, float height){
        this.submitSprite(
            switch(state){
                case DEFAULT -> BUTTON_DEFAULT_SPRITE;
                case DISABLED -> BUTTON_DISABLED_SPRITE;
                case HIGHLIGHTED -> BUTTON_HIGHLIGHTED_SPRITE;
            },
            x, y, width, height, null
        );
    }

    public void submitDefaultSlot(float x, float y, float width, float height){
        this.submitSprite(SLOT_SPRITE, x, y, width, height, null);
    }

    public void submitDefaultSlot(float x, float y){
        this.submitDefaultSlot(x, y, 18, 18);
    }

    public void submitRectangle(float x, float y, float width, float height, Consumer<RectangleProperties> properties){
        // Resolve the properties
        if(this.rectangleProperties == null)
            this.rectangleProperties = new RectangleProperties();
        else
            this.rectangleProperties.clear();
        if(properties != null)
            properties.accept(this.rectangleProperties);

        // Handle centering
        if(this.rectangleProperties.centerHorizontally)
            x -= width / 2f;
        if(this.rectangleProperties.centerVertically)
            y -= height / 2f;

        // Handle border
        if(this.rectangleProperties.border != -1){
            int border = this.rectangleProperties.border;
            int color = this.rectangleProperties.beginColor;
            RenderPipeline renderPipeline = this.rectangleProperties.renderPipeline;
            // Top
            this.guiGraphics.fill(renderPipeline, (int)x, (int)y, (int)(x + width), (int)y + border, color);
            // Bottom
            this.guiGraphics.fill(renderPipeline, (int)x, (int)y - border, (int)(x + width), (int)y, color);
            // Left
            this.guiGraphics.fill(renderPipeline, (int)x, (int)y + border, (int)x + border, (int)(y + height) - border, color);
            // Right
            this.guiGraphics.fill(renderPipeline, (int)x - border, (int)y + border, (int)x, (int)(y + height) - border, color);
            return;
        }

        // Submit rectangle
        this.guiGraphics.innerFill(
            this.rectangleProperties.renderPipeline,
            TextureSetup.noTexture(),
            (int)x, (int)y, (int)(x + width), (int)(y + height),
            this.rectangleProperties.beginColor, this.rectangleProperties.endColor
        );
    }

    void submitRectangle(float x, float y, float width, float height){
        this.submitRectangle(x, y, width, height, null);
    }

    public void submitTooltip(Consumer<TooltipContent> content, float x, float y, Consumer<TooltipProperties> properties){
        // Resolve the content
        if(this.tooltipContent == null)
            this.tooltipContent = new TooltipContent();
        else
            this.tooltipContent.clear();
        content.accept(this.tooltipContent);
        if(this.tooltipContent.content.isEmpty())
            return;

        // Resolve the properties
        if(this.tooltipProperties == null)
            this.tooltipProperties = new TooltipProperties();
        else
            this.tooltipProperties.clear();
        if(properties != null)
            properties.accept(this.tooltipProperties);

        // Submit tooltip
        this.guiGraphics.tooltip(
            this.tooltipProperties.font == null ? ClientUtils.getFontRenderer() : this.tooltipProperties.font,
            List.copyOf(this.tooltipContent.content),
            (int)x, (int)y,
            this.tooltipProperties.positioner,
            this.tooltipProperties.frame
        );
    }

    public void submitTooltip(Consumer<TooltipContent> content, float x, float y){
        this.submitTooltip(content, x, y, null);
    }

    public void submitTooltipForTopStratum(Consumer<TooltipContent> content, float x, float y, Consumer<TooltipProperties> properties){
        // Resolve the content
        if(this.tooltipContent == null)
            this.tooltipContent = new TooltipContent();
        else
            this.tooltipContent.clear();
        content.accept(this.tooltipContent);
        if(this.tooltipContent.content.isEmpty())
            return;

        // Resolve the properties
        if(this.tooltipProperties == null)
            this.tooltipProperties = new TooltipProperties();
        else
            this.tooltipProperties.clear();
        if(properties != null)
            properties.accept(this.tooltipProperties);

        // Submit tooltip
        Matrix3x2f matrix = new Matrix3x2f(this.guiGraphics.pose());
        Font font = this.tooltipProperties.font == null ? ClientUtils.getFontRenderer() : this.tooltipProperties.font;
        List<ClientTooltipComponent> components = List.copyOf(this.tooltipContent.content);
        ClientTooltipPositioner positioner = this.tooltipProperties.positioner;
        Identifier frame = this.tooltipProperties.frame;
        this.guiGraphics.deferredTooltip = () -> {
            this.guiGraphics.pose().pushMatrix().set(matrix);
            this.guiGraphics.tooltip(font, components, (int)x, (int)y, positioner, frame);
            this.guiGraphics.pose().popMatrix();
        };
    }

    public void submitTooltipForTopStratum(Consumer<TooltipContent> content, float x, float y){
        this.submitTooltipForTopStratum(content, x, y, null);
    }

    public void submitItem(ItemStack item, float x, float y, Consumer<ItemProperties> properties){
        if(item.isEmpty())
            return;

        // Resolve the properties
        if(this.itemProperties == null)
            this.itemProperties = new ItemProperties();
        else
            this.itemProperties.clear();
        if(properties != null)
            properties.accept(this.itemProperties);

        // Submit the item
        TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
        if(!this.itemProperties.allowOversized)
            renderState.setOversizedInGui(false);
        ClientUtils.getMinecraft().getItemModelResolver().updateForTopItem(
            renderState,
            item,
            this.itemProperties.displayContext,
            this.itemProperties.level,
            this.itemProperties.entity,
            0
        );
        this.guiGraphics.guiRenderState.addItem(new GuiItemRenderState(
            new Matrix3x2f(this.guiGraphics.pose()),
            renderState,
            (int)x, (int)y,
            this.guiGraphics.scissorStack.peek()
        ));

        // Submit the decorations
        if(this.itemProperties.showBar)
            this.guiGraphics.itemBar(item, (int)x, (int)y);
        if(this.itemProperties.showCooldown)
            this.guiGraphics.itemCooldown(item, (int)x, (int)y);
        if(this.itemProperties.showCount)
            this.guiGraphics.itemCount(ClientUtils.getFontRenderer(), item, (int)x, (int)y, null);
    }

    public void submitItem(ItemStack item, float x, float y){
        this.submitItem(item, x, y, null);
    }

    public void submitGuiElement(GuiElementRenderState element){
        this.guiGraphics.guiRenderState.addGuiElement(element);
    }

    public void submitPictureInPicture(PictureInPictureRenderState element){
        this.guiGraphics.guiRenderState.addPicturesInPictureState(element);
    }

    public void submitFeatures(int x, int y, int width, int height, BiConsumer<PoseStack,SubmitNodeCollector> submitter){
        this.guiGraphics.guiRenderState.addPicturesInPictureState(new ArbitraryPictureInPictureRenderer.State(
            x, y, width, height,
            new Matrix3x2f(this.guiGraphics.pose()),
            this.guiGraphics.scissorStack.peek(),
            submitter
        ));
    }

    public static final class TextProperties {
        private Font font;
        private int color, backgroundColor;
        private boolean shadow;
        private boolean centerHorizontally, centerVertically;
        private Integer wrapWidth, cutoffWidth;
        private Component cutoffPostfix;

        private TextProperties(){
            this.clear();
        }

        private void clear(){
            this.defaultFont();
            this.defaultColor();
            this.noBackground();
            this.shadow(false);
            this.centerHorizontally(false);
            this.centerVertically(false);
            this.wrapWidth = null;
            this.cutoffWidth = null;
            this.cutoffPostfix = null;
        }

        public TextProperties font(Font font){
            this.font = font;
            return this;
        }

        public TextProperties defaultFont(){
            return this.font(null);
        }

        public TextProperties color(int color){
            this.color = color | (255 << 24);
            return this;
        }

        public TextProperties color(int red, int green, int blue){
            return this.color(ARGB.color(red, green, blue));
        }

        public TextProperties color(int red, int green, int blue, int alpha){
            return this.color(ARGB.color(alpha, red, green, blue));
        }

        public TextProperties defaultColor(){
            return this.color(4210752);
        }

        public TextProperties activeColor(){
            return this.color(14737632);
        }

        public TextProperties inactiveColor(){
            return this.color(7368816);
        }

        public TextProperties backgroundColor(int backgroundColor){
            this.backgroundColor = backgroundColor;
            return this;
        }

        public TextProperties noBackground(){
            return this.backgroundColor(0);
        }

        public TextProperties shadow(boolean shadow){
            this.shadow = shadow;
            return this;
        }

        public TextProperties shadow(){
            return this.shadow(true);
        }

        public TextProperties centerHorizontally(boolean center){
            this.centerHorizontally = center;
            return this;
        }

        public TextProperties centerHorizontally(){
            return this.centerHorizontally(true);
        }

        public TextProperties centerVertically(boolean center){
            this.centerVertically = center;
            return this;
        }

        public TextProperties centerVertically(){
            return this.centerVertically(true);
        }

        public TextProperties center(){
            this.centerHorizontally();
            return this.centerVertically();
        }

        public TextProperties wrap(int maxWidth){
            if(maxWidth < 0)
                maxWidth = 0;
            this.wrapWidth = maxWidth;
            this.cutoffWidth = null;
            this.cutoffPostfix = null;
            return this;
        }

        public TextProperties cutoff(int maxWidth, Component postfix){
            if(maxWidth < 0)
                maxWidth = 0;
            this.cutoffWidth = maxWidth;
            this.cutoffPostfix = postfix;
            this.wrapWidth = null;
            return this;
        }

        public TextProperties cutoff(int maxWidth){
            return this.cutoff(maxWidth, null);
        }

        public TextProperties cutoffWithDots(int maxWidth){
            return this.cutoff(maxWidth, Component.literal("..."));
        }
    }

    public static final class TextureProperties {
        private RenderPipeline renderPipeline;
        private float u, v, w, h;
        private int color;
        private boolean centerHorizontally, centerVertically;

        private TextureProperties(){
            this.clear();
        }

        private void clear(){
            this.defaultPipeline();
            this.fullUV();
            this.color(-1);
            this.centerHorizontally(false);
            this.centerVertically(false);
        }

        public TextureProperties renderPipeline(RenderPipeline renderPipeline){
            this.renderPipeline = renderPipeline;
            return this;
        }

        public TextureProperties defaultPipeline(){
            return this.renderPipeline(RenderPipelines.GUI_TEXTURED);
        }

        public TextureProperties uv(float u, float v, float width, float height){
            this.u = u;
            this.v = v;
            this.w = width;
            this.h = height;
            return this;
        }

        public TextureProperties fullUV(){
            return this.uv(0, 0, 1, 1);
        }

        public TextureProperties color(int color){
            this.color = color;
            return this;
        }

        public TextureProperties color(int red, int green, int blue){
            return this.color(ARGB.color(red, green, blue));
        }

        public TextureProperties color(int red, int green, int blue, int alpha){
            return this.color(ARGB.color(alpha, red, green, blue));
        }

        public TextureProperties centerHorizontally(boolean center){
            this.centerHorizontally = center;
            return this;
        }

        public TextureProperties centerHorizontally(){
            return this.centerHorizontally(true);
        }

        public TextureProperties centerVertically(boolean center){
            this.centerVertically = center;
            return this;
        }

        public TextureProperties centerVertically(){
            return this.centerVertically(true);
        }

        public TextureProperties center(){
            this.centerHorizontally();
            return this.centerVertically();
        }
    }

    public enum ButtonState {
        DEFAULT, HIGHLIGHTED, DISABLED
    }

    public static final class RectangleProperties {
        private RenderPipeline renderPipeline;
        private int beginColor, endColor;
        private boolean centerHorizontally, centerVertically;
        private int border;

        private RectangleProperties(){
            this.clear();
        }

        private void clear(){
            this.defaultPipeline();
            this.color(-1);
            this.centerHorizontally(false);
            this.centerVertically(false);
            this.border = -1;
        }

        public RectangleProperties renderPipeline(RenderPipeline renderPipeline){
            this.renderPipeline = renderPipeline;
            return this;
        }

        public RectangleProperties defaultPipeline(){
            return this.renderPipeline(RenderPipelines.GUI);
        }

        public RectangleProperties color(int beginColor, int endColor){
            this.beginColor = beginColor;
            this.endColor = endColor;
            return this;
        }

        public RectangleProperties color(int color){
            return this.color(color, color);
        }

        public RectangleProperties color(int red, int green, int blue){
            return this.color(ARGB.color(red, green, blue));
        }

        public RectangleProperties color(int red, int green, int blue, int alpha){
            return this.color(ARGB.color(alpha, red, green, blue));
        }

        public RectangleProperties centerHorizontally(boolean center){
            this.centerHorizontally = center;
            return this;
        }

        public RectangleProperties centerHorizontally(){
            return this.centerHorizontally(true);
        }

        public RectangleProperties centerVertically(boolean center){
            this.centerVertically = center;
            return this;
        }

        public RectangleProperties centerVertically(){
            return this.centerVertically(true);
        }

        public RectangleProperties center(){
            this.centerHorizontally();
            return this.centerVertically();
        }

        public RectangleProperties unfilled(int borderThickness){
            this.border = borderThickness;
            return this;
        }
    }

    public static class TooltipContent {
        private final List<ClientTooltipComponent> content = new ArrayList<>(10);

        private TooltipContent(){
        }

        private void clear(){
            this.content.clear();
        }

        public TooltipContent add(Collection<ClientTooltipComponent> components){
            this.content.addAll(components);
            return this;
        }

        public TooltipContent add(ClientTooltipComponent... components){
            return this.add(Arrays.asList(components));
        }

        public TooltipContent text(Collection<Component> components){
            this.content.addAll(components.stream().map(c -> ClientTooltipComponent.create(c.getVisualOrderText())).toList());
            return this;
        }

        public TooltipContent text(Component... components){
            return this.text(Arrays.asList(components));
        }

        public TooltipContent literal(Collection<String> s){
            this.content.addAll(s.stream().map(s2 -> ClientTooltipComponent.create(FormattedCharSequence.forward(s2, Style.EMPTY))).toList());
            return this;
        }

        public TooltipContent literal(String... s){
            return this.literal(Arrays.asList(s));
        }

        public TooltipContent synced(Collection<TooltipComponent> components){
            this.content.addAll(components.stream().map(ClientTooltipComponent::create).toList());
            return this;
        }

        public TooltipContent synced(TooltipComponent... components){
            return this.synced(Arrays.asList(components));
        }
    }

    public static class TooltipProperties {
        private ClientTooltipPositioner positioner;
        private Font font;
        private Identifier frame;

        private TooltipProperties(){
            this.clear();
        }

        private void clear(){
            this.defaultPositioner();
            this.defaultFont();
            this.defaultFrame();
        }

        public TooltipProperties positioner(ClientTooltipPositioner positioner){
            this.positioner = positioner;
            return this;
        }

        public TooltipProperties defaultPositioner(){
            return this.positioner(DefaultTooltipPositioner.INSTANCE);
        }

        public TooltipProperties aboveOrBelow(float x, float y, float width, float height){
            return this.positioner(new BelowOrAboveWidgetTooltipPositioner(new ScreenRectangle((int)x, (int)y, (int)width, (int)height)));
        }

        public TooltipProperties font(Font font){
            this.font = font;
            return this;
        }

        public TooltipProperties defaultFont(){
            return this.font(null);
        }

        public TooltipProperties frame(Identifier texture){
            this.frame = texture;
            return this;
        }

        public TooltipProperties defaultFrame(){
            return this.frame(null);
        }
    }

    public static final class ItemProperties {
        private Level level;
        private LivingEntity entity;
        private boolean allowOversized;
        private boolean showBar, showCount, showCooldown;
        private ItemDisplayContext displayContext;

        private ItemProperties(){
            this.clear();
        }

        private void clear(){
            this.level = null;
            this.entity = null;
            this.allowOversized = true;
            this.showBar = true;
            this.showCount = true;
            this.showCooldown = true;
            this.guiDisplayContext();
        }

        public ItemProperties level(Level level){
            this.level = level;
            return this;
        }

        public ItemProperties entity(LivingEntity entity){
            this.entity = entity;
            return this;
        }

        public ItemProperties allowOversized(boolean allowOversized){
            this.allowOversized = allowOversized;
            return this;
        }

        public ItemProperties forceNoOversized(){
            return this.allowOversized(false);
        }

        public ItemProperties showBar(boolean showBar){
            this.showBar = showBar;
            return this;
        }

        public ItemProperties noBar(){
            return this.showBar(false);
        }

        public ItemProperties showCount(boolean showCount){
            this.showCount = showCount;
            return this;
        }

        public ItemProperties noCount(){
            return this.showCount(false);
        }

        public ItemProperties showCooldown(boolean showCooldown){
            this.showCooldown = showCooldown;
            return this;
        }

        public ItemProperties noCooldown(){
            return this.showCooldown(false);
        }

        public ItemProperties noDecorations(){
            this.showBar(false);
            this.showCount(false);
            return this.showCooldown(false);
        }

        public ItemProperties displayContext(ItemDisplayContext displayContext){
            this.displayContext = displayContext;
            return this;
        }

        public ItemProperties guiDisplayContext(){
            return this.displayContext(ItemDisplayContext.GUI);
        }
    }
}
