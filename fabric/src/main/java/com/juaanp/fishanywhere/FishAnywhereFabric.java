package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.config.ConfigHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FishAnywhereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Inicialización común
        CommonClass.init();
        
        // Registrar eventos del ciclo de vida del servidor
        registerLifecycleEvents();
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
}
