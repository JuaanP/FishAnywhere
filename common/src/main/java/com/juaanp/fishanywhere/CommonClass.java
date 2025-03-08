package com.juaanp.fishanywhere;

import com.juaanp.fishanywhere.platform.Services;

public class CommonClass {
    public static void init() {
        if (Services.PLATFORM.isModLoaded("fishanywhere")) {}
    }
}