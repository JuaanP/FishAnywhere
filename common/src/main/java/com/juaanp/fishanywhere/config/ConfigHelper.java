package com.juaanp.fishanywhere.config;

import com.juaanp.fishanywhere.Constants;
import com.juaanp.fishanywhere.platform.Services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Clase auxiliar para manejar la configuración del mod
 */
public class ConfigHelper {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final AtomicBoolean AUTO_SAVE_ENABLED = new AtomicBoolean(true);
    private static final long AUTO_SAVE_INTERVAL_MS = 300000;
    private static long lastSaveTime = 0;
    
    /**
     * Inicializa la configuración del mod
     */
    public static void initialize() {
        if (!INITIALIZED.getAndSet(true)) {
            Constants.LOG.info("Initializing FishAnywhere config...");
            
            try {
                // Cargar configuración del archivo pero no forzar actualización de fluidos
                Services.PLATFORM.loadConfig();
                Constants.LOG.info("Configuration loaded successfully");
            } catch (Exception e) {
                Constants.LOG.error("Failed to load configuration, using defaults", e);
                CommonConfig.getInstance().resetToDefaults();
                
                try {
                    Constants.LOG.info("Creating new config file with defaults...");
                    Services.PLATFORM.saveConfig();
                } catch (Exception ex) {
                    Constants.LOG.error("Failed to create default configuration file", ex);
                }
            }
            
            if (AUTO_SAVE_ENABLED.get()) {
                startAutoSave();
            }
        }
    }

    /**
     * Guarda la configuración si hay cambios
     */
    public static void save() {
        try {
            if (CommonConfig.getInstance().isDirty()) {
                Constants.LOG.debug("Saving configuration changes...");
                Services.PLATFORM.saveConfig();
                CommonConfig.getInstance().markClean();
                lastSaveTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Constants.LOG.error("Error saving configuration", e);
            // Crear copia de seguridad en caso de error
            createBackup();
        }
    }
    
    /**
     * Crea una copia de seguridad de la configuración actual
     */
    private static void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Services.PLATFORM.createConfigBackup("backup_" + timestamp);
            Constants.LOG.info("Configuration backup created successfully");
        } catch (Exception e) {
            Constants.LOG.error("Failed to create configuration backup", e);
        }
    }
    
    /**
     * Inicia el proceso de auto-guardado en un hilo separado
     */
    private static void startAutoSave() {
        Thread autoSaveThread = new Thread(() -> {
            Constants.LOG.debug("Config auto-save thread started");
            while (AUTO_SAVE_ENABLED.get()) {
                try {
                    Thread.sleep(5000); // Comprobar cada 5 segundos
                    
                    // Si han pasado más de AUTO_SAVE_INTERVAL_MS desde el último guardado
                    // y hay cambios sin guardar, guardar la configuración
                    long currentTime = System.currentTimeMillis();
                    if (CommonConfig.getInstance().isDirty() && 
                        (currentTime - lastSaveTime) > AUTO_SAVE_INTERVAL_MS) {
                        save();
                    }
                } catch (InterruptedException e) {
                    Constants.LOG.debug("Config auto-save thread interrupted");
                    break;
                } catch (Exception e) {
                    Constants.LOG.error("Error in config auto-save thread", e);
                }
            }
            Constants.LOG.debug("Config auto-save thread stopped");
        }, "FishAnywhere-ConfigAutoSave");
        
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
    }
    
    /**
     * Detiene el auto-guardado
     */
    public static void disableAutoSave() {
        AUTO_SAVE_ENABLED.set(false);
    }
    
    /**
     * Comprueba si estamos en un entorno de desarrollo
     */
    public static boolean isDevelopmentEnvironment() {
        return Services.PLATFORM.isDevelopmentEnvironment();
    }
    
    /**
     * Recarga la configuración desde el archivo
     */
    public static void reload() {
        try {
            Constants.LOG.info("Reloading configuration...");
            Services.PLATFORM.loadConfig();
            Constants.LOG.info("Configuration reloaded successfully");
        } catch (Exception e) {
            Constants.LOG.error("Failed to reload configuration", e);
        }
    }
}