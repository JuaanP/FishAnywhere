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
        // Este método sólo debe ejecutarse durante la inicialización del servidor, no al abrir la configuración
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Constants.LOG.info("Verifying fluid registry completeness...");
            
            // Actualizar el registro de fluidos pero no forzarlo en la configuración
            FluidRegistryHelper.forceInitialize();
            
            // Sólo si la configuración es nueva o está vacía, cargar los fluidos
            if (CommonConfig.getInstance().getAllowedFluids().isEmpty() || 
                CommonConfig.getInstance().getAllowedFluids().size() <= 1) {
                Constants.LOG.info("New configuration detected, updating with fluid registry...");
                CommonConfig.getInstance().forceLoadAllFluids();
                ConfigHelper.save();
            }
        });
    }
}
