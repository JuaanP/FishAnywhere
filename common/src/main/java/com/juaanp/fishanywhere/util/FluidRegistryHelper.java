package com.juaanp.fishanywhere.util;

import com.juaanp.fishanywhere.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase helper que proporciona acceso centralizado al registro de fluidos
 */
public class FluidRegistryHelper {
    private static final Map<ResourceLocation, Fluid> VALID_FLUIDS = new ConcurrentHashMap<>();
    private static final Map<String, List<Fluid>> FLUIDS_BY_MOD = new ConcurrentHashMap<>();
    private static boolean initialized = false;
    
    /**
     * Inicializa el registro de fluidos de forma forzada, ignorando el estado previo
     */
    public static void forceInitialize() {
        initialized = false;
        initialize();
    }
    
    /**
     * Inicializa el registro de fluidos
     */
    public static void initialize() {
        if (initialized && !VALID_FLUIDS.isEmpty()) {
            return;
        }
        
        // Limpiar los mapas
        VALID_FLUIDS.clear();
        FLUIDS_BY_MOD.clear();
        
        try {
            int count = 0;
            // Imprimir todos los fluidos disponibles para diagnóstico
            Constants.LOG.debug("===== Scanning available fluids =====");
            for (Fluid fluid : BuiltInRegistries.FLUID) {
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                Constants.LOG.debug("Found fluid: {} ({})", id, fluid);
                count++;
            }
            Constants.LOG.debug("Total fluids found in BuiltInRegistries: {}", count);
            
            // Recorrer todos los fluidos en el registro
            BuiltInRegistries.FLUID.forEach(fluid -> {
                // Excluir el fluido vacío y los fluidos "flowing"
                if (fluid != Fluids.EMPTY && 
                    fluid != Fluids.FLOWING_WATER && 
                    fluid != Fluids.FLOWING_LAVA &&
                    !BuiltInRegistries.FLUID.getKey(fluid).getPath().startsWith("flowing_")) {
                    
                    // Obtener el namespace (mod ID)
                    ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                    String modId = fluidId.getNamespace();
                    
                    // Casos especiales: agrupar fluidos específicos bajo "minecraft"
                    if ("milk".equals(modId) || "milk".equals(fluidId.getPath())) {
                        modId = "minecraft";
                    }
                    
                    // Agregar el fluido al mapa central
                    VALID_FLUIDS.put(fluidId, fluid);
                    
                    // Agregar el fluido a la lista del mod correspondiente
                    FLUIDS_BY_MOD.computeIfAbsent(modId, k -> new ArrayList<>()).add(fluid);
                }
            });
            
            // Ordenar las listas de fluidos por mod
            for (List<Fluid> fluidList : FLUIDS_BY_MOD.values()) {
                fluidList.sort(Comparator.comparing(fluid -> BuiltInRegistries.FLUID.getKey(fluid).getPath()));
            }
            
            Constants.LOG.info("FluidRegistryHelper initialized with {} valid fluids from {} mods", 
                    VALID_FLUIDS.size(), FLUIDS_BY_MOD.size());
                
            // Registrar fluidos individuales para diagnóstico
            VALID_FLUIDS.keySet().forEach(id -> 
                Constants.LOG.debug("Registered valid fluid: {}", id));
        } catch (Exception e) {
            Constants.LOG.error("Error initializing FluidRegistryHelper", e);
        }
        
        initialized = true;
    }
    
    /**
     * Obtiene todos los fluidos válidos
     */
    public static Collection<Fluid> getAllFluids() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableCollection(VALID_FLUIDS.values());
    }
    
    /**
     * Obtiene todos los IDs de fluidos válidos
     */
    public static Set<ResourceLocation> getAllFluidIds() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableSet(VALID_FLUIDS.keySet());
    }
    
    /**
     * Obtiene los fluidos organizados por mod
     */
    public static Map<String, List<Fluid>> getFluidsByMod() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableMap(FLUIDS_BY_MOD);
    }
    
    /**
     * Comprueba si un fluido es válido para nuestra aplicación
     */
    public static boolean isValidFluid(Fluid fluid) {
        if (!initialized) {
            initialize();
        }
        
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        return VALID_FLUIDS.containsKey(id);
    }
    
    /**
     * Comprueba si un ID de fluido es válido para nuestra aplicación
     */
    public static boolean isValidFluidId(ResourceLocation id) {
        if (!initialized) {
            initialize();
        }
        
        return id != null && VALID_FLUIDS.containsKey(id);
    }
} 