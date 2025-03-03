package com.juaanp.seamlesstrading;

import com.juaanp.seamlesstrading.client.ConfigScreenBase;
import com.juaanp.seamlesstrading.platform.NeoForgePlatformHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.ModLoadingContext;

@Mod(Constants.MOD_ID)
public class SeamlessTradingNeoForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    public SeamlessTradingNeoForge(IEventBus modEventBus) {
        modEventBus.register(ClientModEvents.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NeoForgePlatformHelper.SPEC);
        CommonClass.init();
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("SeamlessTrading client setup...");

            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, screen) -> new ConfigScreenBase(screen, Minecraft.getInstance().options)
                    )
            );
        }
    }
}