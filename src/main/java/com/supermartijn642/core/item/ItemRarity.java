package com.supermartijn642.core.item;

import net.minecraft.item.Rarity;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public enum ItemRarity {
    COMMON(Rarity.COMMON),
    UNCOMMON(Rarity.UNCOMMON),
    RARE(Rarity.RARE),
    EPIC(Rarity.EPIC);

    private final Rarity rarity;

    ItemRarity(Rarity rarity){
        this.rarity = rarity;
    }

    @Deprecated
    public Rarity getUnderlying(){
        return this.rarity;
    }
}
