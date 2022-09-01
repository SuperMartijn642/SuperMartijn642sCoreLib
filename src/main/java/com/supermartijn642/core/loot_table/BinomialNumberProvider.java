package com.supermartijn642.core.loot_table;

import net.minecraft.world.storage.loot.RandomValueRange;

import java.util.Random;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
public class BinomialNumberProvider extends RandomValueRange {

    private final int n;
    private final float p;

    public BinomialNumberProvider(int n, float p){
        super(0);
        this.n = n;
        this.p = p;
    }

    public int getN(){
        return this.n;
    }

    public float getP(){
        return this.p;
    }

    @Override
    public int generateInt(Random random){
        int value = 0;

        for(int attempt = 0; attempt < this.n; attempt++){
            if(random.nextFloat() < this.p)
                value++;
        }

        return value;
    }

    @Override
    public float generateFloat(Random random){
        return this.generateInt(random);
    }
}
