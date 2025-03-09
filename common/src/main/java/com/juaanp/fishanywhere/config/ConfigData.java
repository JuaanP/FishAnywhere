package com.juaanp.fishanywhere.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Clase para serialización/deserialización de configuración
 * Esta clase debe contener solo tipos simples para facilitar la serialización
 */
public class ConfigData {
    // Versión de la configuración para manejar migraciones
    public int version = 1;
    
    // Opciones de configuración
    public boolean forceOpenWater = CommonConfig.getDefaultForceOpenWater();
    
    // Lista de IDs de fluidos permitidos como strings
    public Set<String> allowedFluids = new HashSet<>();
    
    /**
     * Aplica esta configuración al objeto CommonConfig
     */
    public void applyTo(CommonConfig config) {
        config.setForceOpenWater(forceOpenWater);
        
        // Convertir strings a ResourceLocation
        Set<ResourceLocation> fluidIds = new HashSet<>();
        for (String fluidId : allowedFluids) {
            try {
                fluidIds.add(new ResourceLocation(fluidId));
            } catch (Exception e) {
                // Ignorar IDs inválidos
            }
        }
        
        config.setAllowedFluids(fluidIds);
    }
    
    /**
     * Crea un objeto ConfigData a partir de CommonConfig
     */
    public static ConfigData fromConfig(CommonConfig config) {
        ConfigData data = new ConfigData();
        data.forceOpenWater = config.forceOpenWater();
        
        // Convertir ResourceLocation a strings
        data.allowedFluids.clear();
        for (ResourceLocation fluidId : config.getAllowedFluids()) {
            data.allowedFluids.add(fluidId.toString());
        }
        
        return data;
    }
} 