package com.juaanp.fishanywhere.client;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // Asegurarnos de que el registro de fluidos esté inicializado antes de abrir la pantalla
            Constants.LOG.debug("Opening config screen - ensuring fluid registry is up to date");
            FluidRegistryHelper.forceInitialize();
            
            // Si hay muy pocos fluidos registrados, intentar cargarlos todos
            if (CommonConfig.getInstance().getAllowedFluids().size() <= 2) {
                Constants.LOG.info("Loading all available fluids into configuration");
                CommonConfig.getInstance().forceLoadAllFluids();
            }
            
            // Devolver la pantalla de configuración
            return new ModConfigScreen(parent);
        };
    }
}