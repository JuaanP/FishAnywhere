package com.juaanp.fishanywhere.config;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.HashSet;
import java.util.Set;

import static com.juaanp.fishanywhere.Constants.DEFAULT_FORCE_OPEN_WATER;

public class CommonConfig {
    private static CommonConfig instance;

    private boolean forceOpenWater;
    
    private Set<ResourceLocation> allowedFluids;

    private CommonConfig() {
        this.forceOpenWater = DEFAULT_FORCE_OPEN_WATER;
        
        // Inicializar con fluidos predeterminados (agua por defecto)
        this.allowedFluids = new HashSet<>();
        this.allowedFluids.add(Registry.FLUID.getKey(Fluids.WATER));
    }

    public static CommonConfig getInstance() {
        if (instance == null) {
            instance = new CommonConfig();
        }
        return instance;
    }

    public static boolean getDefaultForceOpenWater() {
        return DEFAULT_FORCE_OPEN_WATER;
    }

    public boolean forceOpenWater() {
        return forceOpenWater;
    }

    public void setForceOpenWater(boolean forceOpenWater) {
        this.forceOpenWater = forceOpenWater;
    }
    
    /**
     * Comprueba si un fluido está en la lista de permitidos
     * @param fluid El fluido a comprobar
     * @return true si el fluido está permitido, false en caso contrario
     */
    public boolean isFluidAllowed(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        
        ResourceLocation fluidId = Registry.FLUID.getKey(fluid);
        return allowedFluids.contains(fluidId);
    }
    
    /**
     * Comprueba si un fluido está en la lista de permitidos por su ID
     * @param fluidId El ID del fluido a comprobar
     * @return true si el fluido está permitido, false en caso contrario
     */
    public boolean isFluidAllowed(ResourceLocation fluidId) {
        if (fluidId == null) {
            return false;
        }
        
        return allowedFluids.contains(fluidId);
    }
    
    /**
     * Establece si un fluido está habilitado o no
     * @param fluidId El ID del fluido
     * @param enabled true para permitir el fluido, false para deshabilitarlo
     */
    public void setFluidEnabled(ResourceLocation fluidId, boolean enabled) {
        if (fluidId == null) {
            return;
        }
        
        if (enabled) {
            allowedFluids.add(fluidId);
        } else {
            allowedFluids.remove(fluidId);
        }
    }
    
    /**
     * Establece si un fluido está habilitado o no
     * @param fluid El fluido
     * @param enabled true para permitir el fluido, false para deshabilitarlo
     */
    public void setFluidEnabled(Fluid fluid, boolean enabled) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        
        ResourceLocation fluidId = Registry.FLUID.getKey(fluid);
        setFluidEnabled(fluidId, enabled);
    }
    
    /**
     * Obtiene el conjunto de IDs de fluidos permitidos
     * @return Conjunto de ResourceLocation con los IDs de fluidos permitidos
     */
    public Set<ResourceLocation> getAllowedFluids() {
        return new HashSet<>(allowedFluids); // Devuelve una copia para evitar modificaciones directas
    }
    
    /**
     * Establece el conjunto completo de fluidos permitidos
     * @param allowedFluids Conjunto de ResourceLocation con los IDs de fluidos a permitir
     */
    public void setAllowedFluids(Set<ResourceLocation> allowedFluids) {
        this.allowedFluids = new HashSet<>(allowedFluids); // Hace una copia para evitar cambios externos
    }
}