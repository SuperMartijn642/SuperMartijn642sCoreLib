package com.supermartijn642.core.render;

import net.minecraft.resources.ResourceLocation;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class TextureAtlases {

    private static final ResourceLocation BLOCKS = ResourceLocation.parse("textures/atlas/blocks.png");
    private static final ResourceLocation PARTICLES = ResourceLocation.parse("textures/atlas/particles.png");
    private static final ResourceLocation MOB_EFFECTS = ResourceLocation.parse("textures/atlas/mob_effects.png");
    private static final ResourceLocation PAINTINGS = ResourceLocation.parse("textures/atlas/paintings.png");
    private static final ResourceLocation SHULKER_BOXES = ResourceLocation.parse("textures/atlas/shulker_boxes.png");
    private static final ResourceLocation BEDS = ResourceLocation.parse("textures/atlas/beds.png");
    private static final ResourceLocation BANNERS = ResourceLocation.parse("textures/atlas/banner_patterns.png");
    private static final ResourceLocation SHIELDS = ResourceLocation.parse("textures/atlas/shield_patterns.png");
    private static final ResourceLocation SIGNS = ResourceLocation.parse("textures/atlas/signs.png");
    private static final ResourceLocation CHESTS = ResourceLocation.parse("textures/atlas/chest.png");

    public static ResourceLocation getBlocks(){
        return BLOCKS;
    }

    public static ResourceLocation getParticles(){
        return PARTICLES;
    }

    public static ResourceLocation getMobEffects(){
        return MOB_EFFECTS;
    }

    public static ResourceLocation getPaintings(){
        return PAINTINGS;
    }

    public static ResourceLocation getShulkerBoxes(){
        return SHULKER_BOXES;
    }

    public static ResourceLocation getBeds(){
        return BEDS;
    }

    public static ResourceLocation getBanners(){
        return BANNERS;
    }

    public static ResourceLocation getShields(){
        return SHIELDS;
    }

    public static ResourceLocation getSigns(){
        return SIGNS;
    }

    public static ResourceLocation getChests(){
        return CHESTS;
    }
}
