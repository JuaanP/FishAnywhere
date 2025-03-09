package com.juaanp.fishanywhere.platform;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue FORCE_OPEN_WATER;
    public static final ForgeConfigSpec SPEC;

    static {
        FORCE_OPEN_WATER = BUILDER
                .comment(".config.forceOpenWater.tooltip")
                .translation(Constants.MOD_ID + ".config.forceOpenWater")
                .define("forceOpenWater", CommonConfig.getDefaultForceOpenWater());

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
        common.setForceOpenWater(FORCE_OPEN_WATER.get());
    }

    private void saveToForgeConfig() {
        CommonConfig common = CommonConfig.getInstance();
        FORCE_OPEN_WATER.set(common.forceOpenWater());
    }
}