package com.juaanp.fishanywhere.config;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuración común compartida entre Fabric y Forge
 */
public class CommonConfig {
    private static final CommonConfig INSTANCE = new CommonConfig();
    
    // Opciones de configuración
    private boolean forceOpenWater;
    private Set<ResourceLocation> allowedFluids;
    
    // Indicador de si la configuración ha sido modificada
    private boolean dirty = false;
    
    // Indicador de si los fluidos por defecto ya fueron cargados
    private boolean defaultFluidsLoaded = false;

    /**
     * Constructor privado para el patrón singleton
     */
    private CommonConfig() {
        // Inicialización básica con valores por defecto
        this.forceOpenWater = Constants.DEFAULT_FORCE_OPEN_WATER;
        this.allowedFluids = new HashSet<>();
        // Siempre incluir agua como mínimo
        this.allowedFluids.add(BuiltInRegistries.FLUID.getKey(Fluids.WATER));
    }
    
    /**
     * Restaura todos los valores a sus configuraciones por defecto
     */
    public void resetToDefaults() {
        this.forceOpenWater = Constants.DEFAULT_FORCE_OPEN_WATER;
        
        this.allowedFluids = new HashSet<>();
        
        // Cargar todos los fluidos disponibles
        loadAllFluids();
        
        this.dirty = true;
    }
    
    /**
     * Carga todos los fluidos disponibles en el registro
     * Esta función se puede llamar en diferentes momentos del ciclo de vida del juego
     */
    public void loadAllFluids() {
        if (defaultFluidsLoaded) {
            return; // Evitar cargar múltiples veces
        }
        
        // Inicializar FluidRegistryHelper si no se ha hecho
        FluidRegistryHelper.initialize();
        
        // Si la configuración está sucia (modificada), no sobrescribir los fluidos permitidos
        // Esto permite que configuraciones existentes mantengan su selección
        if (!this.dirty) {
            // Obtener todos los IDs de fluidos válidos
            Set<ResourceLocation> fluidIds = FluidRegistryHelper.getAllFluidIds();
            
            // Añadir todos los fluidos a la lista de permitidos
            this.allowedFluids.addAll(fluidIds);
            
            Constants.LOG.info("Loaded {} fluid(s) into allowed fluids list", fluidIds.size());
            this.dirty = true;
        } else {
            Constants.LOG.debug("Configuration already modified, skipping automatic fluid loading");
        }
        
        defaultFluidsLoaded = true;
    }

    /**
     * Fuerza la carga de todos los fluidos disponibles, independientemente del estado
     * Esta función se usa principalmente cuando se genera una configuración nueva
     */
    public void forceLoadAllFluids() {
        // Forzar reinicialización del registro de fluidos
        FluidRegistryHelper.forceInitialize();
        
        // Verificar si tenemos suficientes fluidos registrados
        Set<ResourceLocation> fluidIds = FluidRegistryHelper.getAllFluidIds();
        
        if (fluidIds.size() <= 2) {
            // Añadir solo agua por ahora como mínimo
            this.allowedFluids.clear();
            this.allowedFluids.add(BuiltInRegistries.FLUID.getKey(Fluids.WATER));
            
            // Marcar como no cargado para forzar una recarga posterior
            defaultFluidsLoaded = false;
        } else {
            // Tenemos suficientes fluidos, proceder normalmente
            this.allowedFluids.clear();
            this.allowedFluids.addAll(fluidIds);
            Constants.LOG.info("Forced loading of {} fluid(s) into allowed fluids list", fluidIds.size());
            defaultFluidsLoaded = true;
        }
        
        this.dirty = true;
    }

    /**
     * Obtiene la instancia única de la configuración
     */
    public static CommonConfig getInstance() {
        return INSTANCE;
    }

    /**
     * Obtiene el valor predeterminado para forceOpenWater
     */
    public static boolean getDefaultForceOpenWater() {
        return Constants.DEFAULT_FORCE_OPEN_WATER;
    }

    /**
     * Comprueba si el modo "Open Water" está forzado
     */
    public boolean forceOpenWater() {
        return forceOpenWater;
    }

    /**
     * Establece si se debe forzar el modo "Open Water"
     */
    public void setForceOpenWater(boolean forceOpenWater) {
        if (this.forceOpenWater != forceOpenWater) {
            this.forceOpenWater = forceOpenWater;
            this.dirty = true;
        }
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
        
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
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
        
        boolean changed;
        if (enabled) {
            changed = allowedFluids.add(fluidId);
        } else {
            changed = allowedFluids.remove(fluidId);
        }
        
        if (changed) {
            this.dirty = true;
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
        
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        setFluidEnabled(fluidId, enabled);
    }
    
    /**
     * Obtiene el conjunto de IDs de fluidos permitidos
     * @return Conjunto de ResourceLocation con los IDs de fluidos permitidos
     */
    public Set<ResourceLocation> getAllowedFluids() {
        return Collections.unmodifiableSet(allowedFluids);
    }
    
    /**
     * Establece el conjunto completo de fluidos permitidos
     * @param allowedFluids Conjunto de ResourceLocation con los IDs de fluidos a permitir
     */
    public void setAllowedFluids(Set<ResourceLocation> allowedFluids) {
        if (allowedFluids == null) {
            return;
        }
        
        // Verificar si realmente hay cambios
        if (!this.allowedFluids.equals(allowedFluids)) {
            this.allowedFluids = new HashSet<>(allowedFluids);
            this.dirty = true;
        }
    }
    
    /**
     * Comprueba si la configuración ha sido modificada desde la última carga/guardado
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Marca la configuración como guardada (no modificada)
     */
    public void markClean() {
        this.dirty = false;
    }
}