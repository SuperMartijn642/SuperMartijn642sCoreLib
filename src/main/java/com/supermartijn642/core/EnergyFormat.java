package com.supermartijn642.core;

import net.minecraft.client.resources.language.LanguageManager;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created 7/30/2021 by SuperMartijn642
 */
public class EnergyFormat {

    private static EnergyType type = EnergyType.RF;

    /**
     * Cycles through the available energy types.
     */
    public static void cycleEnergyType(boolean forward){
        type = EnergyType.values()[(type.ordinal() + (forward ? 1 : EnergyType.values().length - 1)) % EnergyType.values().length];
    }

    /**
     * Formats an amount of energy for displaying to players. For input {@code 10000} this would result in {@code "10,000"}.
     */
    public static String formatEnergy(int energy){
        return type.convertEnergy(energy);
    }

    /**
     * Formats an amount of energy including the unit for displaying to players. For input {@code 100} this would result in {@code "100 X"}.
     */
    public static String formatEnergyWithUnit(int energy){
        return formatEnergy(energy) + " " + formatUnit();
    }

    /**
     * Formats an amount of energy including the unit per tick for displaying to players. For input {@code 100} this would result in {@code "100 X/t"}.
     */
    public static String formatEnergyPerTick(int energy){
        return formatEnergy(energy) + " " + formatUnitPerTick();
    }

    /**
     * Formats a given amount of energy and capacity for displaying to players. For inputs {@code 5} and {@code 100} this would result in {@code "5 / 100"}.
     */
    public static String formatCapacity(int energy, int capacity){
        return formatEnergy(energy) + " / " + formatEnergy(capacity);
    }

    /**
     * Formats a given amount of energy and capacity including the unit for displaying to players. For inputs {@code 5} and {@code 100} this would result in {@code "5 / 100 X"}.
     */
    public static String formatCapacityWithUnit(int energy, int capacity){
        return formatCapacity(energy, capacity) + " " + formatUnit();
    }

    /**
     * Formats the selected energy type's unit for displaying to players.
     */
    public static String formatUnit(){
        return type.getUnit();
    }

    /**
     * Formats the selected energy type's unit per tick for displaying to players.
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
            LanguageManager manager = ClientUtils.getMinecraft().getLanguageManager();
            Locale locale = manager == null || manager.getSelected() == null ? Locale.getDefault() : manager.getJavaLocale();
            return NumberFormat.getNumberInstance(locale).format(energy);
        }
    }
}
