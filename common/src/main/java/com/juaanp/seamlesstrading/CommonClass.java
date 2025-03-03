package com.juaanp.seamlesstrading;

import com.juaanp.seamlesstrading.platform.Services;

public class CommonClass {
    public static void init() {
        if (Services.PLATFORM.isModLoaded("seamlesstrading")) {}
    }
}