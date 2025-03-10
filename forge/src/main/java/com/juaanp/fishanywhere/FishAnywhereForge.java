package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.client.ModConfigScreen;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.config.ConfigHelper;
import com.juaanp.fishanywhere.platform.ForgePlatformHelper;
import com.juaanp.fishanywhere.util.FluidRegistryHelper;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

@Mod(Constants.MOD_ID)
public class FishAnywhereForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    public FishAnywhereForge() {
        MinecraftForge.EVENT_BUS.register(this);
        CommonClass.init();
        
        // Inicializar el registro de fluidos para diagnóstico temprano
        FluidRegistryHelper.initialize();
    }
    
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        Constants.LOG.debug("Forge server started, ensuring configuration is up to date...");
        ConfigHelper.reload();
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Constants.LOG.debug("Forge server stopping, saving configuration...");
        ConfigHelper.save();
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            LOGGER.info(Constants.MOD_NAME +" common setup...");
            
            // Aquí los registros ya deberían estar completos
            event.enqueueWork(() -> {
                Constants.LOG.info("Updating fluid registry in common setup phase...");
                
                // Forzar la reinicialización del registro de fluidos
                FluidRegistryHelper.forceInitialize();
                
                // Verificar si la configuración está vacía o casi vacía
                if (CommonConfig.getInstance().getAllowedFluids().isEmpty() || 
                    CommonConfig.getInstance().getAllowedFluids().size() <= 1) {
                    Constants.LOG.info("New or empty configuration detected, updating with fluid registry...");
                    CommonConfig.getInstance().forceLoadAllFluids();
                    
                    // Guardar la configuración actualizada
                    ConfigHelper.save();
                }
            });
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info(Constants.MOD_NAME +" client setup...");

            // Registrar pantalla de configuración sin manipular fluidos
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, screen) -> {
                                // Simplemente devolver la pantalla sin manipular fluidos
                                Constants.LOG.debug("Opening config screen");
                                return new ModConfigScreen(screen);
                            }
                    )
            );
        }
    }
}