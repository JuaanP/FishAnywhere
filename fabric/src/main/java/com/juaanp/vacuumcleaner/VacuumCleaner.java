package com.juaanp.vacuumcleaner;

import net.fabricmc.api.ModInitializer;

public class VacuumCleaner implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
    }
}
