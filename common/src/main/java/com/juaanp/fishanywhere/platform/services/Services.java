package com.juaanp.fishanywhere.platform.services;

import com.juaanp.fishanywhere.platform.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    
    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to load service for " + clazz.getName()));
    }
}