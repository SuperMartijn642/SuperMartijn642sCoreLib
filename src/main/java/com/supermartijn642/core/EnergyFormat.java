package com.supermartijn642.core;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created 7/30/2021 by SuperMartijn642
 */
public class EnergyFormat {

    private static EnergyType type = EnergyType.RF;

    /**
     * Cycles through the available energy types
     */
    public static void cycleEnergyType(boolean forward){
        type = EnergyType.values()[(type.ordinal() + (forward ? 1 : EnergyType.values().length - 1)) % EnergyType.values().length];
    }

    /**
     * Formats an amount energy for displaying to players
     */
    public static String formatEnergy(int energy){
        return type.convertEnergy(energy);
    }

    /**
     * Formats the selected energy type's unit for displaying to players
     */
    public static String formatUnit(){
        return type.getUnit();
    }

    /**
     * Formats the selected energy type's unit per tick for displaying to players
     */
    public static String formatUnitPerTick(){
        return type.unit + "/t";
    }

    private enum EnergyType {
        FE("FE"), RF("RF")/*, MJ("MJ")*/;

        private final String unit;

        EnergyType(String unit){
            this.unit = unit;
        }

        public String getUnit(){
            return this.unit;
        }

        public String convertEnergy(int energy){
            return NumberFormat.getNumberInstance(Locale.getDefault()).format(energy);
        }
    }
}
