package com.supermartijn642.core.render;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class TextureAtlases {

    private static final ResourceLocation BLOCKS = AtlasTexture.LOCATION_BLOCKS;
    private static final ResourceLocation PARTICLES = AtlasTexture.LOCATION_PARTICLES;
    private static final ResourceLocation MOB_EFFECTS = new ResourceLocation("textures/atlas/mob_effects.png");
    private static final ResourceLocation PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");
    private static final ResourceLocation SHULKER_BOXES = Atlases.SHULKER_SHEET;
    private static final ResourceLocation BEDS = Atlases.BED_SHEET;
    private static final ResourceLocation BANNERS = Atlases.BANNER_SHEET;
    private static final ResourceLocation SHIELDS = Atlases.SHIELD_SHEET;
    private static final ResourceLocation SIGNS = Atlases.SIGN_SHEET;
    private static final ResourceLocation CHESTS = Atlases.CHEST_SHEET;

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
