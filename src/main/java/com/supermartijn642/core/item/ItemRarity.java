package com.supermartijn642.core.item;

import net.minecraft.item.EnumRarity;
import net.minecraftforge.common.IRarity;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public enum ItemRarity {
    COMMON(EnumRarity.COMMON),
    UNCOMMON(EnumRarity.UNCOMMON),
    RARE(EnumRarity.RARE),
    EPIC(EnumRarity.EPIC);

    private final IRarity rarity;

    ItemRarity(IRarity rarity){
        this.rarity = rarity;
    }

    @Deprecated
    public IRarity getUnderlying(){
        return this.rarity;
    }
}
