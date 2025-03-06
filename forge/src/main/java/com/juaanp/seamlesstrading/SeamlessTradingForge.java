package com.juaanp.seamlesstrading;

import com.juaanp.seamlesstrading.platform.ForgePlatformHelper;

@Mod(Constants.MOD_ID)
public class SeamlessTradingForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    public SeamlessTradingForge() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgePlatformHelper.SPEC);
        CommonClass.init();
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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