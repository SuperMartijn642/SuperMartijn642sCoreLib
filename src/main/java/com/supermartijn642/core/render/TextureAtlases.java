package com.supermartijn642.core.render;

import net.minecraft.resources.Identifier;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public class TextureAtlases {

    private static final Identifier BLOCKS = Identifier.parse("textures/atlas/blocks.png");
    private static final Identifier PARTICLES = Identifier.parse("textures/atlas/particles.png");
    private static final Identifier MOB_EFFECTS = Identifier.parse("textures/atlas/mob_effects.png");
    private static final Identifier PAINTINGS = Identifier.parse("textures/atlas/paintings.png");
    private static final Identifier SHULKER_BOXES = Identifier.parse("textures/atlas/shulker_boxes.png");
    private static final Identifier BEDS = Identifier.parse("textures/atlas/beds.png");
    private static final Identifier BANNERS = Identifier.parse("textures/atlas/banner_patterns.png");
    private static final Identifier SHIELDS = Identifier.parse("textures/atlas/shield_patterns.png");
    private static final Identifier SIGNS = Identifier.parse("textures/atlas/signs.png");
    private static final Identifier CHESTS = Identifier.parse("textures/atlas/chest.png");
    private static final Identifier GUI = Identifier.parse("textures/atlas/gui.png");

    public static Identifier getBlocks(){
        return BLOCKS;
    }

    public static Identifier getParticles(){
        return PARTICLES;
    }

    public static Identifier getMobEffects(){
        return MOB_EFFECTS;
    }

    public static Identifier getPaintings(){
        return PAINTINGS;
    }

    public static Identifier getShulkerBoxes(){
        return SHULKER_BOXES;
    }

    public static Identifier getBeds(){
        return BEDS;
    }

    public static Identifier getBanners(){
        return BANNERS;
    }

    public static Identifier getShields(){
        return SHIELDS;
    }

    public static Identifier getSigns(){
        return SIGNS;
    }

    public static Identifier getChests(){
        return CHESTS;
    }

    public static Identifier getGUI(){
        return GUI;
    }
}
