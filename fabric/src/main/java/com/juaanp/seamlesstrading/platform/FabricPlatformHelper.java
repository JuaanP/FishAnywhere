package com.juaanp.seamlesstrading.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.juaanp.seamlesstrading.Constants;
import com.juaanp.seamlesstrading.config.CommonConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FabricPlatformHelper implements IPlatformHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID + ".json").toFile();

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void loadConfig() {
        try {
            if (CONFIG_FILE.exists()) {
                try (FileReader reader = new FileReader(CONFIG_FILE)) {
                    ConfigData config = GSON.fromJson(reader, ConfigData.class);
                    applyConfig(config);
                }
            } else {
                saveConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        try {
            if (!CONFIG_FILE.exists() && !CONFIG_FILE.createNewFile()) {
                return;
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                ConfigData config = createConfigData();
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyConfig(ConfigData config) {
        CommonConfig common = CommonConfig.getInstance();
        common.setForceOpenWater(config.forceOpenWater);
        common.setWaterEnabled(config.waterEnabled);
        common.setLavaEnabled(config.lavaEnabled);
        common.setEmptyEnabled(config.emptyEnabled);
        common.setOtherFluidsEnabled(config.otherFluidsEnabled);
        Map<ResourceLocation, Boolean> fluidMap = new HashMap<>();
        config.fluidStates.forEach((key, value) -> {
            fluidMap.put(ResourceLocation.parse(key), value);
        });
        common.setFluidStates(fluidMap);
    }

    private ConfigData createConfigData() {
        ConfigData config = new ConfigData();
        CommonConfig common = CommonConfig.getInstance();
        config.forceOpenWater = common.forceOpenWater();
        config.waterEnabled = common.isWaterEnabled();
        config.lavaEnabled = common.isLavaEnabled();
        config.emptyEnabled = common.isEmptyEnabled();
        config.otherFluidsEnabled = common.isOtherFluidsEnabled();
        Map<String, Boolean> serializedFluidStates = new HashMap<>();
        common.getFluidStates().forEach((key, value) -> {
            serializedFluidStates.put(key.toString(), value);
        });
        config.fluidStates = serializedFluidStates;
        return config;
    }

    private static class ConfigData {
        boolean forceOpenWater = CommonConfig.getDefaultForceOpenWater();
        boolean waterEnabled = CommonConfig.getDefaultWaterEnabled();
        boolean lavaEnabled = CommonConfig.getDefaultLavaEnabled();
        boolean emptyEnabled = CommonConfig.getDefaultEmptyEnabled();
        boolean otherFluidsEnabled = CommonConfig.getDefaultOtherFluidsEnabled();
        Map<String, Boolean> fluidStates = new HashMap<>();
    }
}