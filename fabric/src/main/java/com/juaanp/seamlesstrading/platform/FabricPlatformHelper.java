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
        common.setScrollNewOffers(config.scrollNewOffers);
    }

    private ConfigData createConfigData() {
        ConfigData config = new ConfigData();
        CommonConfig common = CommonConfig.getInstance();
        config.scrollNewOffers = common.isScrollNewOffers();
        return config;
    }

    private static class ConfigData {
        boolean scrollNewOffers = CommonConfig.getDefaultScrollNewOffers();
    }
}