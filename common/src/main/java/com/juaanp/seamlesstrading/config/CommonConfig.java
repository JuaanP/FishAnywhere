package com.juaanp.seamlesstrading.config;

import com.juaanp.seamlesstrading.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.juaanp.seamlesstrading.Constants.*;

public class CommonConfig {
    private static CommonConfig instance;

    private boolean forceOpenWater;
    private boolean waterEnabled;
    private boolean lavaEnabled;
    private boolean emptyEnabled;
    private boolean otherFluidsEnabled;
    
    // Nuevo mapa para almacenar el estado de cada fluido individual
    private Map<ResourceLocation, Boolean> fluidStates;

    // Private constructor for singleton
    private CommonConfig() {
        // Default values
        this.forceOpenWater = DEFAULT_FORCE_OPEN_WATER;
        this.waterEnabled = DEFAULT_WATER_ENABLED;
        this.lavaEnabled = DEFAULT_LAVA_ENABLED;
        this.emptyEnabled = DEFAULT_EMPTY_ENABLED;
        this.otherFluidsEnabled = DEFAULT_OTHER_FLUIDS_ENABLED;
        this.fluidStates = new HashMap<>();
    }

    public static CommonConfig getInstance() {
        if (instance == null) {
            instance = new CommonConfig();
        }
        return instance;
    }

    // Default getters
    public static boolean getDefaultForceOpenWater() {
        return DEFAULT_FORCE_OPEN_WATER;
    }

    public static boolean getDefaultWaterEnabled() {
        return DEFAULT_WATER_ENABLED;
    }

    public static boolean getDefaultLavaEnabled() {
        return DEFAULT_LAVA_ENABLED;
    }

    public static boolean getDefaultEmptyEnabled() {
        return DEFAULT_EMPTY_ENABLED;
    }

    public static boolean getDefaultOtherFluidsEnabled() {
        return DEFAULT_OTHER_FLUIDS_ENABLED;
    }

    public boolean forceOpenWater() {
        return forceOpenWater;
    }

    public void setForceOpenWater(boolean forceOpenWater) {
        this.forceOpenWater = forceOpenWater;
    }

    public boolean isWaterEnabled() {
        return waterEnabled;
    }

    public void setWaterEnabled(boolean waterEnabled) {
        this.waterEnabled = waterEnabled;
    }

    public boolean isLavaEnabled() {
        return lavaEnabled;
    }

    public void setLavaEnabled(boolean lavaEnabled) {
        this.lavaEnabled = lavaEnabled;
    }

    public boolean isEmptyEnabled() {
        return emptyEnabled;
    }

    public void setEmptyEnabled(boolean emptyEnabled) {
        this.emptyEnabled = emptyEnabled;
    }

    public boolean isOtherFluidsEnabled() {
        return otherFluidsEnabled;
    }

    public void setOtherFluidsEnabled(boolean otherFluidsEnabled) {
        this.otherFluidsEnabled = otherFluidsEnabled;
    }

    public void setFluidEnabled(ResourceLocation fluidId, boolean enabled) {
        fluidStates.put(fluidId, enabled);
    }

    public Map<ResourceLocation, Boolean> getFluidStates() {
        return fluidStates;
    }

    public void setFluidStates(Map<ResourceLocation, Boolean> fluidStates) {
        this.fluidStates = fluidStates;
    }

    // Método usado por el mixin
    public boolean isFluidAllowed(Fluid fluid) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        
        // Si el fluido tiene una configuración específica, úsala
        if (fluidStates.containsKey(fluidId)) {
            return fluidStates.get(fluidId);
        }
        
        // Si no, usa la configuración de categoría
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return waterEnabled;
        } else if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return lavaEnabled;
        } else if (fluid == Fluids.EMPTY) {
            return emptyEnabled;
        } else {
            return otherFluidsEnabled;
        }
    }
}