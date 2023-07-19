package com.supermartijn642.core.render;


import net.minecraft.util.ResourceLocation;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class TextureAtlases {

    private static final ResourceLocation BLOCKS = new ResourceLocation("textures/atlas/blocks.png");
    private static final ResourceLocation PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private static final ResourceLocation MOB_EFFECTS = new ResourceLocation("textures/atlas/mob_effects.png");
    private static final ResourceLocation PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");
    private static final ResourceLocation SHULKER_BOXES = new ResourceLocation("textures/atlas/shulker_boxes.png");
    private static final ResourceLocation BEDS = new ResourceLocation("textures/atlas/beds.png");
    private static final ResourceLocation BANNERS = new ResourceLocation("textures/atlas/banner_patterns.png");
    private static final ResourceLocation SHIELDS = new ResourceLocation("textures/atlas/shield_patterns.png");
    private static final ResourceLocation SIGNS = new ResourceLocation("textures/atlas/signs.png");
    private static final ResourceLocation CHESTS = new ResourceLocation("textures/atlas/chest.png");

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
