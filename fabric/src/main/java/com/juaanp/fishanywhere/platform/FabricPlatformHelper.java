package com.juaanp.fishanywhere.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.config.ConfigData;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FabricPlatformHelper implements IPlatformHelper {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    
    // Archivo de configuración principal
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve(Constants.MOD_ID + ".json");
    
    // Directorio para copias de seguridad
    private static final Path BACKUP_DIR = CONFIG_DIR.resolve(Constants.MOD_ID + "_backups");

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
        boolean configExists = Files.exists(CONFIG_FILE);
        
        try {
            if (configExists) {
                try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    ConfigData configData = GSON.fromJson(reader, ConfigData.class);
                    
                    if (configData != null) {
                        configData.applyTo(CommonConfig.getInstance());
                        Constants.LOG.info("Configuration loaded from JSON file");
                        
                        // Marcar la configuración como limpia ya que acabamos de cargarla
                        CommonConfig.getInstance().markClean();
                    }
                }
            } else {
                Constants.LOG.info("Config file not found, creating default configuration");
                
                // Inicializar FluidRegistryHelper y cargar todos los fluidos, ya que es la primera vez
                FluidRegistryHelper.initialize();
                CommonConfig.getInstance().loadAllFluids();
                
                saveConfig();
            }
        } catch (Exception e) {
            Constants.LOG.error("Error loading configuration", e);
            createConfigBackup("error");
        }
    }

    @Override
    public void saveConfig() {
        try {
            // Asegurarse de que el directorio existe
            Files.createDirectories(CONFIG_FILE.getParent());
            
            // Crear un archivo temporal primero para evitar corrupción si hay un cierre inesperado
            Path tempFile = CONFIG_FILE.resolveSibling(CONFIG_FILE.getFileName() + ".tmp");
            
            // Serializar la configuración
            ConfigData configData = ConfigData.fromConfig(CommonConfig.getInstance());
            
            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                GSON.toJson(configData, writer);
            }
            
            // Mover el archivo temporal al archivo real (operación atómica)
            Files.move(tempFile, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            
            Constants.LOG.debug("Configuration saved successfully to {}", CONFIG_FILE);
        } catch (IOException e) {
            Constants.LOG.error("Error saving configuration", e);
        }
    }

    @Override
    public void createConfigBackup(String backupName) {
        try {
            // Asegurarse de que el directorio de backups existe
            Files.createDirectories(BACKUP_DIR);
            
            // Crear nombre de archivo para la copia de seguridad
            Path backupFile = BACKUP_DIR.resolve(Constants.MOD_ID + "_" + backupName + ".json");
            
            // Copiar el archivo actual si existe
            if (Files.exists(CONFIG_FILE)) {
                Files.copy(CONFIG_FILE, backupFile, StandardCopyOption.REPLACE_EXISTING);
                Constants.LOG.info("Created configuration backup: {}", backupFile);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to create configuration backup", e);
        }
    }
}