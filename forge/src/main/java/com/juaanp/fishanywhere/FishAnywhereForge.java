package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.client.ModConfigScreen;
import com.juaanp.fishanywhere.platform.ForgePlatformHelper;

import java.util.logging.Logger;

@Mod(Constants.MOD_ID)
public class FishAnywhereForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    public FishAnywhereForge() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgePlatformHelper.SPEC);
        CommonClass.init();
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info(Constants.MOD_NAME + " client setup...");

            ModLoadingContext.get().registerExtensionPoint(
                    ExtensionPoint.CONFIGGUIFACTORY,
                    () -> (minecraft, screen) -> new ModConfigScreen(screen, Minecraft.getInstance().options)
            );
        }
    }
}