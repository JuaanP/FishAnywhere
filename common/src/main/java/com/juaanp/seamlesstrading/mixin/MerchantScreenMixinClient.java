package com.juaanp.seamlesstrading.mixin;

import com.juaanp.seamlesstrading.config.CommonConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixinClient {
    @Unique CommonConfig seamlessTrading$config = CommonConfig.getInstance();
    @Unique private int seamlessTrading$previousOfferSize = 0;

    @Shadow int scrollOff;
    @Shadow @Final private static int NUMBER_OF_OFFER_BUTTONS;

    @Inject(
            method = "renderScroller",
            at = @At(value = "TAIL")
    )
    private void autoScroll(GuiGraphics guiGraphics, int i, int j, MerchantOffers merchantOffers, CallbackInfo ci) {
        if(!seamlessTrading$config.isScrollNewOffers()){return;}

        int offerSize = merchantOffers.size();
        if(seamlessTrading$previousOfferSize != offerSize) {
            if(seamlessTrading$previousOfferSize >= NUMBER_OF_OFFER_BUTTONS - 1){
                scrollOff = offerSize - NUMBER_OF_OFFER_BUTTONS;
            }
            seamlessTrading$previousOfferSize = offerSize;
        }
    }
}