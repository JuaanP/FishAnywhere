package com.juaanp.fishanywhere.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.config.CommonConfig;
import com.juaanp.fishanywhere.config.ConfigData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgePlatformHelper implements IPlatformHelper {
    // Configuración Forge para integración con la GUI de Forge
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    
    // Opción forceOpenWater para config Forge
    public static final ForgeConfigSpec.BooleanValue FORCE_OPEN_WATER = BUILDER
            .comment("Controls whether fishing should always be treated as in open water.")
            .translation(Constants.MOD_ID + ".config.forceOpenWater")
            .define("forceOpenWater", CommonConfig.getDefaultForceOpenWater());
    
    // Lista de fluidos permitidos para config Forge
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_FLUIDS = BUILDER
            .comment("List of fluids where fishing is allowed.")
            .translation(Constants.MOD_ID + ".config.allowedFluids")
            .defineList("allowedFluids", 
                    () -> List.of("minecraft:water"),
                    obj -> obj instanceof String && ResourceLocation.isValidResourceLocation((String) obj));
    
    // Especificación completa
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    
    // Para manejo de JSON para compatibilidad con Fabric
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    
    // Archivo de configuración en formato JSON (para compatibilidad)
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve(Constants.MOD_ID + ".json");
    
    // Directorio para copias de seguridad
    private static final Path BACKUP_DIR = CONFIG_DIR.resolve(Constants.MOD_ID + "_backups");

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
        // Primero intentamos cargar desde la configuración de Forge
        applyForgeConfig();
        
        // Luego intentamos cargar desde el archivo JSON para compatibilidad con Fabric
        // Esto permite transferir configs entre versiones Fabric y Forge
        tryLoadFromJson();
        
        // Al final, actualizamos la configuración de Forge con los valores actuales
        updateForgeConfig();
    }

    @Override
    public void saveConfig() {
        // Actualizar la configuración de Forge
        updateForgeConfig();
        
        // Guardar también en formato JSON para compatibilidad
        try {
            // Asegurarse de que el directorio existe
            Files.createDirectories(CONFIG_FILE.getParent());
            
            // Crear un archivo temporal primero
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
            Constants.LOG.error("Error saving configuration to JSON", e);
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
    
    /**
     * Aplica la configuración de Forge a CommonConfig
     */
    private void applyForgeConfig() {
        try {
            CommonConfig config = CommonConfig.getInstance();
            
            // Aplicar forceOpenWater
            config.setForceOpenWater(FORCE_OPEN_WATER.get());
            
            // Aplicar fluidos permitidos
            Set<ResourceLocation> allowedFluids = new HashSet<>();
            for (String fluidId : ALLOWED_FLUIDS.get()) {
                try {
                    allowedFluids.add(new ResourceLocation(fluidId));
                } catch (Exception e) {
                    Constants.LOG.warn("Invalid fluid ID in config: {}", fluidId);
                }
            }
            
            config.setAllowedFluids(allowedFluids);
            
            Constants.LOG.info("Forge configuration applied successfully");
        } catch (Exception e) {
            Constants.LOG.error("Error applying Forge configuration", e);
        }
    }
    
    /**
     * Actualiza la configuración de Forge con los valores de CommonConfig
     */
    private void updateForgeConfig() {
        try {
            CommonConfig config = CommonConfig.getInstance();
            
            // Actualizar forceOpenWater
            FORCE_OPEN_WATER.set(config.forceOpenWater());
            
            // Actualizar fluidos permitidos
            List<String> fluidIds = config.getAllowedFluids().stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toCollection(ArrayList::new));
            
            ALLOWED_FLUIDS.set(fluidIds);
            
            Constants.LOG.debug("Forge configuration updated successfully");
        } catch (Exception e) {
            Constants.LOG.error("Error updating Forge configuration", e);
        }
    }
    
    /**
     * Intenta cargar la configuración desde el archivo JSON para compatibilidad
     */
    private void tryLoadFromJson() {
        if (!Files.exists(CONFIG_FILE)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            ConfigData configData = GSON.fromJson(reader, ConfigData.class);
            
            if (configData != null) {
                configData.applyTo(CommonConfig.getInstance());
                Constants.LOG.info("Configuration loaded from JSON for compatibility");
            }
        } catch (Exception e) {
            Constants.LOG.error("Error loading configuration from JSON", e);
        }
    }
}