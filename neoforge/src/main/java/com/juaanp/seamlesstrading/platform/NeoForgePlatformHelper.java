package com.juaanp.seamlesstrading.platform;

import com.juaanp.seamlesstrading.Constants;
import com.juaanp.seamlesstrading.config.CommonConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class NeoForgePlatformHelper implements IPlatformHelper {
    // Keep the NeoForge config spec definition here
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    // Nueva configuración para pesca
    public static final ModConfigSpec.BooleanValue FORCE_OPEN_WATER;
    public static final ModConfigSpec.BooleanValue WATER_ENABLED;
    public static final ModConfigSpec.BooleanValue LAVA_ENABLED;
    public static final ModConfigSpec.BooleanValue EMPTY_ENABLED;
    public static final ModConfigSpec.BooleanValue OTHER_FLUIDS_ENABLED;
    public static final ModConfigSpec.ConfigValue<Map<String, Boolean>> FLUID_STATES;
    
    public static final ModConfigSpec SPEC;

    static {
        FORCE_OPEN_WATER = BUILDER
                .comment("seamlesstrading.config.forceOpenWater.tooltip")
                .translation(Constants.MOD_ID + ".config.forceOpenWater")
                .define("forceOpenWater", CommonConfig.getDefaultForceOpenWater());

        WATER_ENABLED = BUILDER
                .comment("seamlesstrading.config.waterEnabled.tooltip")
                .translation(Constants.MOD_ID + ".config.waterEnabled")
                .define("waterEnabled", CommonConfig.getDefaultWaterEnabled());

        LAVA_ENABLED = BUILDER
                .comment("seamlesstrading.config.lavaEnabled.tooltip")
                .translation(Constants.MOD_ID + ".config.lavaEnabled")
                .define("lavaEnabled", CommonConfig.getDefaultLavaEnabled());

        EMPTY_ENABLED = BUILDER
                .comment("seamlesstrading.config.emptyEnabled.tooltip")
                .translation(Constants.MOD_ID + ".config.emptyEnabled")
                .define("emptyEnabled", CommonConfig.getDefaultEmptyEnabled());

        OTHER_FLUIDS_ENABLED = BUILDER
                .comment("seamlesstrading.config.otherFluidsEnabled.tooltip")
                .translation(Constants.MOD_ID + ".config.otherFluidsEnabled")
                .define("otherFluidsEnabled", CommonConfig.getDefaultOtherFluidsEnabled());

        FLUID_STATES = BUILDER
                .comment("seamlesstrading.config.fluidStates.tooltip")
                .translation(Constants.MOD_ID + ".config.fluidStates")
                .define("fluidStates", new HashMap<>());

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
        common.setForceOpenWater(FORCE_OPEN_WATER.get());
        common.setWaterEnabled(WATER_ENABLED.get());
        common.setLavaEnabled(LAVA_ENABLED.get());
        common.setEmptyEnabled(EMPTY_ENABLED.get());
        common.setOtherFluidsEnabled(OTHER_FLUIDS_ENABLED.get());
        Map<ResourceLocation, Boolean> fluidMap = new HashMap<>();
        FLUID_STATES.get().forEach((key, value) -> {
            fluidMap.put(new ResourceLocation(key), value);
        });
        common.setFluidStates(fluidMap);
    }

    private void saveToNeoForgeConfig() {
        CommonConfig common = CommonConfig.getInstance();
        FORCE_OPEN_WATER.set(common.forceOpenWater());
        WATER_ENABLED.set(common.isWaterEnabled());
        LAVA_ENABLED.set(common.isLavaEnabled());
        EMPTY_ENABLED.set(common.isEmptyEnabled());
        OTHER_FLUIDS_ENABLED.set(common.isOtherFluidsEnabled());
        Map<String, Boolean> serializedFluidStates = new HashMap<>();
        common.getFluidStates().forEach((key, value) -> {
            serializedFluidStates.put(key.toString(), value);
        });
        FLUID_STATES.set(serializedFluidStates);
        // No need to call SPEC.save() as NeoForge handles this internally
    }
}