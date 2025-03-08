package com.juaanp.fishanywhere.platform;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgePlatformHelper implements IPlatformHelper {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue SCROLL_NEW_OFFERS;
    public static final ForgeConfigSpec SPEC;

    static {
        SCROLL_NEW_OFFERS = BUILDER
                .comment("fishanywhere.config.scrollNewOffers.tooltip")
                .translation(Constants.MOD_ID + ".config.scrollNewOffers")
                .define("scrollNewOffers", CommonConfig.getDefaultScrollNewOffers());

        SPEC = BUILDER.build();
    }

    @Override
    public String getPlatformName() {
        return "Forge";
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
        saveToForgeConfig();
    }

    private void applyToCommonConfig() {
        CommonConfig common = CommonConfig.getInstance();
        common.setScrollNewOffers(SCROLL_NEW_OFFERS.get());
    }

    private void saveToForgeConfig() {
        CommonConfig common = CommonConfig.getInstance();
        SCROLL_NEW_OFFERS.set(common.isScrollNewOffers());
    }
}