package com.juaanp.fishanywhere.platform;

import com.juaanp.fishanywhere.config.ConfigData;

/**
 * Interfaz para funcionalidades específicas de plataforma
 */
public interface IPlatformHelper {
    /**
     * Obtiene el nombre de la plataforma (Fabric, Forge, etc.)
     */
    String getPlatformName();
    
    /**
     * Comprueba si un mod está cargado
     */
    boolean isModLoaded(String modId);
    
    /**
     * Comprueba si estamos en un entorno de desarrollo
     */
    boolean isDevelopmentEnvironment();

    /**
     * Carga la configuración del mod
     */
    void loadConfig();
    
    /**
     * Guarda la configuración del mod
     */
    void saveConfig();
    
    /**
     * Crea una copia de seguridad de la configuración
     * @param backupName Nombre para el archivo de copia de seguridad
     */
    void createConfigBackup(String backupName);
}