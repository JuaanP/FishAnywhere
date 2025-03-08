package com.juaanp.fishanywhere.config;

import com.juaanp.fishanywhere.platform.Services;

public class ConfigHelper {
    private static boolean isInitialized = false;
    
    public static void initialize() {
        if (!isInitialized) {
            Services.PLATFORM.loadConfig();
            isInitialized = true;
        }
    }
    
    public static void save() {
        Services.PLATFORM.saveConfig();
    }
}