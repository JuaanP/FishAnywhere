package com.juaanp.fish_anywhere;

import com.juaanp.fish_anywhere.platform.Services;

public class CommonClass {

    public static void init() {
        if (Services.PLATFORM.isModLoaded("fish_anywhere")) {
            Constants.LOG.info("Loaded Fish Anywhere");
        }
    }
}