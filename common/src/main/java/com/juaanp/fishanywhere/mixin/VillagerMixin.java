package com.juaanp.fishanywhere.mixin;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Shadow private boolean increaseProfessionLevelOnUpdate;
    @Shadow protected abstract void increaseMerchantCareer();
    @Shadow protected abstract void resendOffersToTradingPlayer();

    @Inject(
            method = "tick",
            at = @At(value = "TAIL")
    )
    private void ignoreMerchantTimer(CallbackInfo ci) {
        if(increaseProfessionLevelOnUpdate){
            increaseMerchantCareer();
            resendOffersToTradingPlayer();
            increaseProfessionLevelOnUpdate = false;
        }
    }
}