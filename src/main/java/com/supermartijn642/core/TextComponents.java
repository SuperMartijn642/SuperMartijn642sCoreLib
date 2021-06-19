package com.supermartijn642.core;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

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
        return new TextComponentBuilder(new TextComponentString(text));
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given translation.
     */
    public static TextComponentBuilder translation(String translationKey, Object... arguments){
        return new TextComponentBuilder(new TextComponentTranslation(translationKey, arguments));
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given translation.
     */
    public static TextComponentBuilder translation(String translationKey){
        return new TextComponentBuilder(new TextComponentTranslation(translationKey));
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given text component.
     */
    public static TextComponentBuilder fromTextComponent(ITextComponent textComponent){
        return new TextComponentBuilder(textComponent);
    }

    /**
     * Formats the given text component. Must only be side client side.
     * @return the formatted string
     */
    public static String format(ITextComponent textComponent){
        return textComponent.getFormattedText();
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given block's name.
     */
    public static TextComponentBuilder block(Block block){
        return translation(block.getUnlocalizedName() + ".name");
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given block state's
     * name.
     */
    public static TextComponentBuilder blockState(IBlockState state){
        return block(state.getBlock());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given item's name.
     */
    public static TextComponentBuilder item(Item item){
        return translation(item.getUnlocalizedName() + ".name");
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given item stack's
     * display name. The display name includes any custom name.
     */
    public static TextComponentBuilder itemStack(ItemStack stack){
        return string(stack.getDisplayName());
    }

    /**
     * Creates a new {@link TextComponentBuilder} around the given entity's
     * display name. The display name includes custom names.
     */
    public static TextComponentBuilder entity(Entity entity){
        return entity.hasCustomName() ? string(entity.getCustomNameTag()) : fromTextComponent(entity.getDisplayName());
    }

    /**
     * Converts the dimension registry name to a capitalized name and creates a
     * new {@link TextComponentBuilder} around it.
     */
    public static TextComponentBuilder dimension(DimensionType dimension){
        String dimensionName = dimension.getName();
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
    public static TextComponentBuilder dimension(World world){
        return dimension(world.provider.getDimensionType());
    }

    public static class TextComponentBuilder {

        private final TextComponentBuilder parent;
        private final ITextComponent textComponent;

        private TextComponentBuilder(ITextComponent textComponent, TextComponentBuilder parent){
            this.textComponent = textComponent;
            this.parent = parent;
        }

        private TextComponentBuilder(ITextComponent textComponent){
            this(textComponent, null);
        }

        /**
         * Sets the formatting for the text component.
         */
        public TextComponentBuilder formatting(TextFormatting color){
            this.updateStyle(style -> style.setColor(color));
            return this;
        }

        /**
         * Sets the formatting for the text component.
         */
        public TextComponentBuilder color(TextFormatting color){
            return this.formatting(color);
        }

        /**
         * Makes the text component <b>bold</b>.
         */
        public TextComponentBuilder bold(){
            this.updateStyle(style -> style.setBold(true));
            return this;
        }

        /**
         * Makes the text component <i>italic<i/>.
         */
        public TextComponentBuilder italic(){
            this.updateStyle(style -> style.setItalic(true));
            return this;
        }

        /**
         * Makes the text component <u>underlined<u/>.
         */
        public TextComponentBuilder underline(){
            this.updateStyle(style -> style.setUnderlined(true));
            return this;
        }

        /**
         * Makes the text component <s>strikethrough<s/>.
         */
        public TextComponentBuilder strikethrough(){
            this.updateStyle(style -> style.setStrikethrough(true));
            return this;
        }

        /**
         * Makes the text component obfuscated, i.e. random characters.
         */
        public TextComponentBuilder obfuscate(){
            this.updateStyle(style -> style.setObfuscated(true));
            return this;
        }

        /**
         * Makes the text component's style.
         */
        public TextComponentBuilder reset(){
            this.updateStyle(style -> new Style().setBold(false).setItalic(false).setUnderlined(false).setStrikethrough(false).setObfuscated(false));
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
            return this.append(new TextComponentString(text));
        }

        /**
         * Appends the given translation to the text component and returns a new
         * {@link TextComponentBuilder} for the given translation.
         * @return a new {@link TextComponentBuilder} for the given translation
         */
        public TextComponentBuilder translation(String translationKey, Object... arguments){
            return this.append(new TextComponentTranslation(translationKey, arguments));
        }

        /**
         * Appends the given translation to the text component and returns a new
         * {@link TextComponentBuilder} for the given translation.
         * @return a new {@link TextComponentBuilder} for the given translation
         */
        public TextComponentBuilder translation(String translationKey){
            return this.append(new TextComponentTranslation(translationKey));
        }

        /**
         * Appends the given translation to the text component and returns a new
         * {@link TextComponentBuilder} for the given translation.
         * @return a new {@link TextComponentBuilder} for the given text component
         */
        public TextComponentBuilder append(ITextComponent textComponent){
            this.textComponent.appendSibling(textComponent);
            return new TextComponentBuilder(textComponent, this);
        }

        /**
         * @return the constructed text component
         */
        public ITextComponent get(){
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
