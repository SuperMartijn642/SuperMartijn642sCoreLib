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
}
