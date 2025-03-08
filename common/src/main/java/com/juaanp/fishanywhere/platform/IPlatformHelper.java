package com.juaanp.fishanywhere.platform;

public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();

    void loadConfig();
    void saveConfig();
}