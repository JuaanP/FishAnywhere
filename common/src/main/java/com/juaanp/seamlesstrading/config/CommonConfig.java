package com.juaanp.seamlesstrading.config;

import static com.juaanp.seamlesstrading.Constants.DEFAULT_SCROLL_NEW_OFFERS;

public class CommonConfig {
    private static CommonConfig instance;

    private boolean scrollNewOffers;

    private CommonConfig() {
        // Default values
        this.scrollNewOffers = DEFAULT_SCROLL_NEW_OFFERS;
    }

    public static CommonConfig getInstance() {
        if (instance == null) {
            instance = new CommonConfig();
        }
        return instance;
    }

    public static boolean getDefaultScrollNewOffers() { return DEFAULT_SCROLL_NEW_OFFERS; }

    public boolean isScrollNewOffers() { return scrollNewOffers; }
    public void setScrollNewOffers(boolean scrollNewOffers) {
        this.scrollNewOffers = scrollNewOffers;
    }
}