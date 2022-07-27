package com.supermartijn642.core.render;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class TextureAtlases {

    private static final ResourceLocation BLOCKS = TextureAtlas.LOCATION_BLOCKS;
    private static final ResourceLocation PARTICLES = TextureAtlas.LOCATION_PARTICLES;
    private static final ResourceLocation MOB_EFFECTS = new ResourceLocation("textures/atlas/mob_effects.png");
    private static final ResourceLocation PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");
    private static final ResourceLocation SHULKER_BOXES = Sheets.SHULKER_SHEET;
    private static final ResourceLocation BEDS = Sheets.BED_SHEET;
    private static final ResourceLocation BANNERS = Sheets.BANNER_SHEET;
    private static final ResourceLocation SHIELDS = Sheets.SHIELD_SHEET;
    private static final ResourceLocation SIGNS = Sheets.SIGN_SHEET;
    private static final ResourceLocation CHESTS = Sheets.CHEST_SHEET;

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
