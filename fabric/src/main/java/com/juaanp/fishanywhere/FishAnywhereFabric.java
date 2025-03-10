package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.config.ConfigHelper;
import com.juaanp.fishanywhere.config.CommonConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;

public class FishAnywhereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Inicialización común
        CommonClass.init();
        
        // Inicializar el registro de fluidos para diagnóstico temprano
        FluidRegistryHelper.initialize();
        
        // Registrar eventos del ciclo de vida del servidor
        registerLifecycleEvents();
        
        // Programar una tarea para actualizar la configuración después de que todos los mods estén cargados
        scheduleConfigUpdate();
    }
    
    private void registerLifecycleEvents() {
        // Guardar configuración al detener el servidor
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Constants.LOG.debug("Server stopping, saving configuration...");
            ConfigHelper.save();
        });
        
        // Al iniciar el servidor, recargar la configuración
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Constants.LOG.debug("Server started, ensuring configuration is up to date...");
            ConfigHelper.reload();
        });
    }
    
    private void scheduleConfigUpdate() {
        // En Fabric, podemos usar ServerLifecycleEvents.SERVER_STARTING para asegurarnos de que
        // todos los registros estén completos
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Constants.LOG.info("Verifying fluid registry completeness...");
            
            // Forzar la reinicialización del registro de fluidos
            FluidRegistryHelper.forceInitialize();
            
            // Verificar si necesitamos actualizar la configuración
            if (CommonConfig.getInstance().getAllowedFluids().size() <= 2) {
                Constants.LOG.info("Updating configuration with complete fluid registry...");
                CommonConfig.getInstance().forceLoadAllFluids();
                
                // Guardar la configuración actualizada
                ConfigHelper.save();
            }
        });
    }
}
