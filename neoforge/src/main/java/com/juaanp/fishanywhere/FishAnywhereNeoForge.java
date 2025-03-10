package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.client.ModConfigScreen;
import com.juaanp.fishanywhere.config.ConfigHelper;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.ModLoadingContext;

@Mod(Constants.MOD_ID)
public class FishAnywhereNeoForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    public FishAnywhereNeoForge(IEventBus modEventBus) {
        // Registrar eventos del bus de mods
        modEventBus.register(this);
        modEventBus.register(ClientModEvents.class);
        
        // Inicialización común
        CommonClass.init();
        
        // Inicializar el registro de fluidos para diagnóstico temprano
        FluidRegistryHelper.initialize();
        
        // Registrar eventos del ciclo de vida del servidor
        registerLifecycleEvents();
    }
    
    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        // Programar una tarea para actualizar la configuración después de que todos los mods estén cargados
        event.enqueueWork(this::scheduleConfigUpdate);
    }
    
    private void registerLifecycleEvents() {
        // Registrar eventos en el bus del juego (NeoForge)
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }
    
    private void onServerStopping(ServerStoppingEvent event) {
        Constants.LOG.debug("Server stopping, saving configuration...");
        ConfigHelper.save();
    }
    
    private void onServerStarted(ServerStartedEvent event) {
        Constants.LOG.debug("Server started, ensuring configuration is up to date...");
        ConfigHelper.reload();
    }
    
    private void onServerStarting(ServerStartingEvent event) {
        scheduleConfigUpdate();
    }
    
    private void scheduleConfigUpdate() {
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
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info(Constants.MOD_NAME + " client setup...");

            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> new IConfigScreenFactory() {
                        @Override
                        public Screen createScreen(ModContainer modContainer, Screen parentScreen) {
                            // Simplemente devolver la pantalla sin manipular fluidos
                            return new ModConfigScreen(parentScreen);
                        }
                    }
            );
        }
    }
}