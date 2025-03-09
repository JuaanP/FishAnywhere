package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.config.ConfigHelper;
import com.juaanp.fishanywhere.platform.Services;

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
}