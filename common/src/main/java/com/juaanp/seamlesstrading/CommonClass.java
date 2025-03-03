package com.juaanp.seamlesstrading;

import com.juaanp.seamlesstrading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

public class CommonClass {
    public static void init() {
        if (Services.PLATFORM.isModLoaded("seamlesstrading")) {}
    }
}