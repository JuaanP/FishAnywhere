package com.juaanp.fishanywhere.platform;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgePlatformHelper implements IPlatformHelper {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    public static final ModConfigSpec.BooleanValue SCROLL_NEW_OFFERS;
    
    public static final ModConfigSpec SPEC;

    static {
        SCROLL_NEW_OFFERS = BUILDER
                .comment("fishanywhere.config.scrollNewOffers.tooltip")
                .translation(Constants.MOD_ID + ".config.scrollNewOffers")
                .define("scrollNewOffers", CommonConfig.getDefaultScrollNewOffers());

        SPEC = BUILDER.build();
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public void loadConfig() {
        applyToCommonConfig();
    }

    @Override
    public void saveConfig() {
        saveToNeoForgeConfig();
    }

    private void applyToCommonConfig() {
        CommonConfig common = CommonConfig.getInstance();
        common.setScrollNewOffers(SCROLL_NEW_OFFERS.get());
    }

    private void saveToNeoForgeConfig() {
        CommonConfig common = CommonConfig.getInstance();
        SCROLL_NEW_OFFERS.set(common.isScrollNewOffers());
    }
}