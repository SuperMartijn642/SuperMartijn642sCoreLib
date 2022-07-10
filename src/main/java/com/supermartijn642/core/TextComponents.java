package com.supermartijn642.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Function;

/**
 * Created 6/9/2021 by SuperMartijn642
 */
public class TextComponents {

    /**
     * Creates a new empty {@link TextComponentBuilder}.
     */
    public static TextComponentBuilder empty(){
        return string("");
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given {@code text}.
     */
    public static TextComponentBuilder string(String text){
        return new TextComponentBuilder(Component.literal(text));
    }

    /**
     * Creates a new {@link TextComponentBuilder} for the given {@code number}.
     */
    public static TextComponentBuilder number(int number){
        return new TextComponentBuilder(Component.literal(Integer.toString(number)));
    }

    /**
     * Creates a new {@link TextComponentBuilder} for the given {@code number}.
     */
    public static TextComponentBuilder number(double number, int decimals){
        return new TextComponentBuilder(Component.literal(String.format("%." + decimals + "f", number)));
    }

    /**
     * Creates a new {@link TextComponentBuilder} for the given {@code number}.
     */
    public static TextComponentBuilder number(double number){
        return new TextComponentBuilder(Component.literal(Double.toString(number)));
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given translation.
     */
    public static TextComponentBuilder translation(String translationKey, Object... arguments){
        return new TextComponentBuilder(Component.translatable(translationKey, arguments));
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given translation.
     */
    public static TextComponentBuilder translation(String translationKey){
        return new TextComponentBuilder(Component.translatable(translationKey));
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given text component.
     */
    public static TextComponentBuilder fromTextComponent(MutableComponent textComponent){
        return new TextComponentBuilder(textComponent);
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given text component.
     */
    public static TextComponentBuilder fromTextComponent(Component textComponent){
        return fromTextComponent(textComponent.plainCopy());
    }

    /**
     * Formats the given text component. Must only be side client side.
     * @return the formatted string
     */
    public static String format(Component textComponent){
        return textComponent.getString();
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given block's name.
     */
    public static TextComponentBuilder block(Block block){
        return translation(block.getDescriptionId());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given block state's
     * name.
     */
    public static TextComponentBuilder blockState(BlockState state){
        return block(state.getBlock());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given item's name.
     */
    public static TextComponentBuilder item(Item item){
        return translation(item.getDescriptionId());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given item stack's
     * display name. The display name includes any custom name.
     */
    public static TextComponentBuilder itemStack(ItemStack stack){
        return fromTextComponent(stack.getHoverName().plainCopy());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given fluid's name.
     */
    public static TextComponentBuilder fluid(Fluid fluid){
        return translation(fluid.getFluidType().getDescriptionId());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given fluid stack's
     * display name.
     */
    public static TextComponentBuilder fluidStack(FluidStack stack){
        return fromTextComponent(stack.getDisplayName());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given entity's
     * display name. The display name includes custom names.
     */
    public static TextComponentBuilder entity(Entity entity){
        return fromTextComponent(entity.hasCustomName() ? entity.getCustomName() : entity.getDisplayName());
    }

    /**
     * Converts the dimension registry name to a capitalized name and creates a
     * new {@link TextComponentBuilder} around it.
     */
    public static TextComponentBuilder dimension(ResourceKey<Level> dimension){
        String dimensionName = dimension.location().getPath();
        dimensionName = dimensionName.substring(Math.min(dimensionName.length() - 1, Math.max(0, dimensionName.indexOf('/') + 1))).toLowerCase();
        dimensionName = dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
        for(int i = 0; i < dimensionName.length() - 1; i++)
            if(dimensionName.charAt(i) == '_' && Character.isAlphabetic(dimensionName.charAt(i + 1)))
                dimensionName = dimensionName.substring(0, i) + ' ' + (i + 2 < dimensionName.length() ? dimensionName.substring(i + 1, i + 2).toUpperCase() + dimensionName.substring(i + 2) : dimensionName.substring(i + 1).toUpperCase());
        return string(dimensionName);
    }

    /**
     * Converts the dimension registry name to a capitalized name and creates a
     * new {@link TextComponentBuilder} around it.
     */
    public static TextComponentBuilder dimension(Level world){
        return dimension(world.dimension());
    }

    public static class TextComponentBuilder {

        private final TextComponentBuilder parent;
        private final MutableComponent textComponent;

        private TextComponentBuilder(MutableComponent textComponent, TextComponentBuilder parent){
            this.textComponent = textComponent;
            this.parent = parent;
        }

        private TextComponentBuilder(MutableComponent textComponent){
            this(textComponent, null);
        }

        /**
         * Sets the formatting for the text component.
         */
        public TextComponentBuilder formatting(ChatFormatting color){
            this.updateStyle(style -> style.withColor(color));
            return this;
        }

        /**
         * Sets the formatting for the text component.
         */
        public TextComponentBuilder color(ChatFormatting color){
            return this.formatting(color);
        }

        /**
         * Makes the text component <b>bold</b>.
         */
        public TextComponentBuilder bold(){
            this.updateStyle(style -> style.withBold(true));
            return this;
        }

        /**
         * Makes the text component <i>italic<i/>.
         */
        public TextComponentBuilder italic(){
            this.updateStyle(style -> style.withItalic(true));
            return this;
        }

        /**
         * Makes the text component <u>underlined<u/>.
         */
        public TextComponentBuilder underline(){
            this.updateStyle(style -> style.withUnderlined(true));
            return this;
        }

        /**
         * Makes the text component <s>strikethrough<s/>.
         */
        public TextComponentBuilder strikethrough(){
            this.updateStyle(style -> style.withStrikethrough(true));
            return this;
        }

        /**
         * Makes the text component obfuscated, i.e. random characters.
         */
        public TextComponentBuilder obfuscate(){
            this.updateStyle(style -> style.withObfuscated(true));
            return this;
        }

        /**
         * Makes the text component's style.
         */
        public TextComponentBuilder reset(){
            this.updateStyle(style -> Style.EMPTY.withBold(false).withItalic(false).withUnderlined(false).withStrikethrough(false).withObfuscated(false));
            return this;
        }

        private void updateStyle(Function<Style,Style> updater){
            this.textComponent.setStyle(updater.apply(this.textComponent.getStyle()));
        }

        /**
         * Appends the given string to the text component and returns a new
         * {@link TextComponentBuilder} for the given string.
         * @return a new {@link TextComponentBuilder} for the given string
         */
        public TextComponentBuilder string(String text){
            return this.append(Component.literal(text));
        }

        /**
         * Appends the given translation to the text component and returns a new
         * {@link TextComponentBuilder} for the given translation.
         * @return a new {@link TextComponentBuilder} for the given translation
         */
        public TextComponentBuilder translation(String translationKey, Object... arguments){
            return this.append(Component.translatable(translationKey, arguments));
        }

        /**
         * Appends the given translation to the text component and returns a new
         * {@link TextComponentBuilder} for the given translation.
         * @return a new {@link TextComponentBuilder} for the given translation
         */
        public TextComponentBuilder translation(String translationKey){
            return this.append(Component.translatable(translationKey));
        }

        /**
         * Appends the given translation to the text component and returns a new
         * {@link TextComponentBuilder} for the given translation.
         * @return a new {@link TextComponentBuilder} for the given text component
         */
        public TextComponentBuilder append(MutableComponent textComponent){
            this.textComponent.append(textComponent);
            return new TextComponentBuilder(textComponent, this);
        }

        /**
         * @return the constructed text component
         */
        public MutableComponent get(){
            return this.parent == null ? this.textComponent : this.parent.get();
        }

        /**
         * Formats the text component. Must only be used client side.
         * @return the constructed text component
         */
        public String format(){
            return TextComponents.format(this.get());
        }
    }

}
