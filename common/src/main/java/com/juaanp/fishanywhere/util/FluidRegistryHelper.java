package com.juaanp.fishanywhere.util;

import com.juaanp.fishanywhere.Constants;
import net.minecraft.core.Registry;
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
     * Inicializa el registro de fluidos
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        VALID_FLUIDS.clear();
        FLUIDS_BY_MOD.clear();
        
        // Recorrer todos los fluidos en el registro
        Registry.FLUID.forEach(fluid -> {
            // Excluir el fluido vacío y los fluidos "flowing"
            if (fluid != Fluids.EMPTY &&
                fluid != Fluids.FLOWING_WATER &&
                fluid != Fluids.FLOWING_LAVA &&
                !Registry.FLUID.getKey(fluid).getPath().startsWith("flowing_")) {

                // Obtener el namespace (mod ID)
                ResourceLocation fluidId = Registry.FLUID.getKey(fluid);
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
            fluidList.sort(Comparator.comparing(fluid -> Registry.FLUID.getKey(fluid).getPath()));
        }
        
        Constants.LOG.info("FluidRegistryHelper initialized fluids from {} mods", FLUIDS_BY_MOD.size());
        
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
        
        ResourceLocation id = Registry.FLUID.getKey(fluid);
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