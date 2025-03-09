package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.config.ConfigHelper;
import com.juaanp.fishanywhere.platform.Services;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;

public class CommonClass {
    private static boolean initialized = false;
    
    public static void init() {
        if (initialized) return;
        
        Constants.LOG.info("Initializing {} mod (common)", Constants.MOD_NAME);
        
        // Inicializar la configuración
        ConfigHelper.initialize();
        
        // Registrar hook para guardar configuración al cerrar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Constants.LOG.debug("Saving configuration during shutdown...");
            ConfigHelper.save();
        }));
        
        initialized = true;
        
        Constants.LOG.info("{} initialization completed", Constants.MOD_NAME);
    }
    
    /**
     * Se llama cuando el registro de fluidos está disponible
     * Esto permite asegurar que la configuración incluye todos los fluidos disponibles
     */
    public static void onFluidsAvailable() {
        Constants.LOG.info("Fluids registry available, initializing registry helper...");
        
        // Solo inicializar el registro de fluidos para tenerlo disponible
        // pero no cargar automáticamente en la configuración
        FluidRegistryHelper.initialize();
        
        // No llamamos a CommonConfig.getInstance().loadAllFluids() aquí
        // porque se hará solo cuando sea necesario (nueva configuración o reset)
    }
}