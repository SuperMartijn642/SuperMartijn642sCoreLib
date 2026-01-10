package com.supermartijn642.core.gui.widget.premade;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.util.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Created 09/01/2026 by SuperMartijn642
 */
public class ScrollbarWidget extends BaseWidget {

    public static Builder builder(int height){
        return new Builder(height);
    }

    private static final ResourceLocation BACKGROUND = new ResourceLocation("supermartijn642corelib", "textures/gui/scrollbar_background.png");
    private static final ResourceLocation SCROLLER = new ResourceLocation("supermartijn642corelib", "textures/gui/scroller.png");

    private final int scrollerHeight;
    private final DoubleSupplier value, minValue, maxValue;
    private final boolean invertScrolling;
    private final Double stepSize;
    private final ScrollListener onChange;
    private final Double scrollerSpeed;
    private final boolean smoothValues;
    private final ResourceLocation background, scroller;
    private final Double scrollWheelStep;
    /**
     * Scrolling position on scale from 0 to 1
     */
    private float scrollerPosition = 0;
    private boolean dragging = false;
    private boolean active = true, scrollable = true;

    private ScrollbarWidget(int x, int y, int width, int height, int scrollerHeight, DoubleSupplier value, DoubleSupplier minValue, DoubleSupplier maxValue, boolean invertScrolling, Double stepSize, ScrollListener onChange, Integer scrollerSpeed, boolean smoothValues, ResourceLocation background, ResourceLocation scroller, Double scrollWheelStep){
        super(x, y, width, height);
        this.scrollerHeight = scrollerHeight;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.invertScrolling = invertScrolling;
        this.stepSize = stepSize;
        this.onChange = onChange;
        this.scrollWheelStep = scrollWheelStep;
        this.scrollerSpeed = scrollerSpeed == null ? null : (double)scrollerSpeed / (height - scrollerHeight);
        this.smoothValues = smoothValues;
        this.background = background;
        this.scroller = scroller;
    }

    public void setActive(boolean active){
        this.active = active;
    }

    public void setScrollable(boolean scrollable){
        this.scrollable = scrollable;
    }

    private boolean canUserMoveScroller(){
        return this.scrollable && this.maxValue.getAsDouble() > this.minValue.getAsDouble();
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("supermartijn642corelib.widgets.scrollbar.narration").get();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        // Update dragging
        if(this.dragging){
            if(!this.canUserMoveScroller())
                this.dragging = false;
            else
                this.updateDrag(mouseY);
        }

        super.renderBackground(context, mouseX, mouseY);

        // Render background
        if(this.background != null){
            ScreenUtils.bindTexture(this.background);
            ScreenUtils.drawTexture(context.poseStack(), this.x - 1, this.y - 1, this.width + 2, this.height + 2);
        }
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        double min = this.minValue.getAsDouble(), max = this.maxValue.getAsDouble();
        double range = max - min;
        if(range <= 0 || !this.active){
            ScreenUtils.bindTexture(this.scroller);
            ScreenUtils.drawTexture(context.poseStack(), this.x, this.y, this.width, this.scrollerHeight, 0, 2 / 3f, 1, 1 / 3f);
            return;
        }

        // Calculate the target scroller position
        double value = Mth.clamp(this.value.getAsDouble() - min, 0, range);
        if(this.stepSize != null)
            value = Math.round((value) / this.stepSize) * this.stepSize;
        float targetPosition = (float)(value / range);
        if(this.invertScrolling)
            targetPosition = 1 - targetPosition;
        // Update the scroller position
        if(this.scrollerSpeed != null)
            this.scrollerPosition = (float)Mth.clamp(targetPosition, this.scrollerPosition - this.scrollerSpeed, this.scrollerPosition + this.scrollerSpeed);
        else
            this.scrollerPosition = targetPosition;
        float offset = (this.height - this.scrollerHeight) * this.scrollerPosition;
        ScreenUtils.bindTexture(this.scroller);
        ScreenUtils.drawTexture(context.poseStack(), this.x, this.y + offset, this.width, this.scrollerHeight, 0, this.scrollable && this.isFocused() ? 1 / 3f : 0, 1, 1 / 3f);
    }

    private void tryScrollTo(double targetPosition, boolean fromScrollWheel){
        targetPosition = Mth.clamp(targetPosition, 0, 1);
        if(this.smoothValues && !fromScrollWheel)
            targetPosition = Mth.clamp(targetPosition, this.scrollerPosition - this.scrollerSpeed, this.scrollerPosition + this.scrollerSpeed);
        double min = this.minValue.getAsDouble(), max = this.maxValue.getAsDouble();
        double range = max - min;
        if(range <= 0)
            return; // Give up
        if(this.invertScrolling)
            targetPosition = 1 - targetPosition;
        double value = targetPosition * range + min;
        if(this.stepSize != null)
            value = min + Math.round((value - min) / this.stepSize) * this.stepSize;
        double oldValue = Mth.clamp(this.value.getAsDouble(), min, max);
        this.onChange.onChange(oldValue, value);
    }

    private void updateDrag(int mouseY){
        this.tryScrollTo((mouseY - this.y - this.scrollerHeight / 2f) / (this.height - this.scrollerHeight), false);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(this.active && this.canUserMoveScroller() && !hasBeenHandled && button == 0 && this.isFocused()){
            this.dragging = true;
            this.updateDrag(mouseY);
            hasBeenHandled = true;
        }
        return super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        if(this.active && this.canUserMoveScroller() && !hasBeenHandled){
            if(this.scrollWheelStep == null)
                this.tryScrollTo(this.scrollerPosition - scrollAmount / 5f, true);
            else if(this.scrollWheelStep != 0){
                double min = this.minValue.getAsDouble(), max = this.maxValue.getAsDouble();
                double range = max - min;
                if(range > 0){
                    double value = Mth.clamp(this.value.getAsDouble(), min, max);
                    if(this.invertScrolling)
                        scrollAmount = -scrollAmount;
                    value += scrollAmount * this.scrollWheelStep;
                    this.tryScrollTo((value - min) / range, true);
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled);
    }

    public static class Builder {

        private int x, y;
        private int width = 12, height;
        private int scrollerHeight = 15;
        private DoubleSupplier value, minValue, maxValue;
        private boolean invertScrolling;
        private Double stepSize;
        private ScrollListener onChange;
        private Integer scrollerSpeed;
        private boolean smoothValues;
        private ResourceLocation background = BACKGROUND, scroller = SCROLLER;
        private Double scrollWheelStep;

        private Builder(int height){
            this.height = height;
            this.scrollRange(0, 0, 1);
        }

        public Builder position(int x, int y){
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width){
            this.width = width;
            return this;
        }

        public Builder height(int height){
            this.height = height;
            return this;
        }

        public Builder size(int width, int height){
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder scrollerHeight(int height){
            this.scrollerHeight = height;
            return this;
        }

        public Builder scrollValue(DoubleSupplier value, DoubleSupplier min, DoubleSupplier max){
            this.value = value;
            this.minValue = min;
            this.maxValue = max;
            return this;
        }

        public Builder scrollValue(DoubleSupplier value, double min, double max){
            if(min > max)
                throw new IllegalArgumentException("Minimum value must be smaller than maximum value!");
            return this.scrollValue(value, () -> min, () -> max);
        }

        public Builder scrollRange(double initialValue, double min, double max){
            if(initialValue < min || initialValue > max)
                throw new IllegalArgumentException("Initial value must be between min and max value!");
            Holder<Double> value = new Holder<>(initialValue);
            return this.scrollValue(value::get, min, max).onChange((oldValue, newValue) -> value.set(newValue));
        }

        public Builder scrollStepSize(double step){
            this.stepSize = step;
            return this;
        }

        /**
         * Sets the maximum change in value per frame.
         * @param speed        maximum number of pixels that the scroller can move per frame
         * @param smoothValues whether to limit the speed at which the underlying value is updated according to the scroller's speed
         */
        public Builder scrollerSpeed(int speed, boolean smoothValues){
            this.scrollerSpeed = speed;
            this.smoothValues = smoothValues;
            return this;
        }

        /**
         * Limits the speed at which the scroller moves to 8 pixels per frame.
         */
        public Builder smoothScrolling(){
            return this.scrollerSpeed(8, true);
        }

        public Builder onChange(ScrollListener onChange){
            this.onChange = onChange;
            return this;
        }

        public Builder onChange(DoubleConsumer onChange){
            return this.onChange((oldValue, newValue) -> onChange.accept(newValue));
        }

        public Builder background(ResourceLocation texture){
            this.background = texture;
            return this;
        }

        public Builder noBackground(){
            return this.background(null);
        }

        public Builder scroller(ResourceLocation texture){
            this.scroller = texture;
            return this;
        }

        public Builder scrollWheelValueChange(double step){
            this.scrollWheelStep = step;
            return this;
        }

        public Builder invertScrolling(){
            this.invertScrolling = true;
            return this;
        }

        public ScrollbarWidget build(){
            if(this.scrollerHeight >= this.height)
                throw new IllegalStateException("Scroller height must be smaller than the height of the scrollbar!");
            return new ScrollbarWidget(
                this.x, this.y, this.width, this.height, this.scrollerHeight,
                this.value, this.minValue, this.maxValue,
                this.invertScrolling, this.stepSize,
                this.onChange,
                this.scrollerSpeed, this.smoothValues,
                this.background, this.scroller,
                this.scrollWheelStep
            );
        }
    }

    public interface ScrollListener {
        void onChange(double oldValue, double newValue);
    }
}
