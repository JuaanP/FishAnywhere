package com.juaanp.fishanywhere.mixin;

import com.juaanp.fishanywhere.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixinForge {
    @Unique private final CommonConfig fishAnywhere$config = CommonConfig.getInstance();

    @Unique
    private FluidState fishAnywhere$fluidState;

    @Unique
    private ParticleOptions fishAnywhere$replaceParticles(ParticleOptions defaultParticles){
        if(fishAnywhere$fluidState == null){return defaultParticles;}

        ParticleOptions particles = fishAnywhere$fluidState.getDripParticle();

        if(
            particles != null
            &&
            !(fishAnywhere$fluidState.is(Fluids.WATER) || fishAnywhere$fluidState.is(Fluids.FLOWING_WATER))
            &&
            fishAnywhere$config.isFluidAllowed(fishAnywhere$fluidState.getType())
        ){
            return particles;
        }

        return defaultParticles;
    }

    @Redirect(method = "getOpenWaterTypeForBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState onFluidCheck(BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();
        if (fishAnywhere$config.forceOpenWater() || fishAnywhere$config.isFluidAllowed(fluidState.getType())) {
            return Fluids.WATER.defaultFluidState();
        }
        return fluidState;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean onFluidStateIs0(FluidState fluidState, TagKey tagKey) {
        fishAnywhere$fluidState = fluidState;
        return fishAnywhere$config.isFluidAllowed(fluidState.getType());
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1))
    private boolean onFluidStateIs1(FluidState fluidState, TagKey tagKey) {
        return fishAnywhere$config.isFluidAllowed(fluidState.getType());
    }

    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean onCatchingFish0(BlockState blockState, Block block) {
        if(fishAnywhere$fluidState == null){
            fishAnywhere$fluidState = blockState.getFluidState();
        }

        return fishAnywhere$config.isFluidAllowed(fishAnywhere$fluidState.getType());
    }

    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 1))
    private boolean onCatchingFish1(BlockState blockState, Block block) {
        if(fishAnywhere$fluidState == null){
            fishAnywhere$fluidState = blockState.getFluidState();
        }

        return fishAnywhere$config.isFluidAllowed(fishAnywhere$fluidState.getType());
    }

    @Inject(method = "calculateOpenWater", at = @At("RETURN"), cancellable = true)
    private void forceOpenWater(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir){
        if(fishAnywhere$config.forceOpenWater()){
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",
            ordinal = 0), index = 0)
    private ParticleOptions replaceFishingParticles0(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",
            ordinal = 1), index = 0)
    private ParticleOptions replaceFishingParticles1(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",
            ordinal = 2), index = 0)
    private ParticleOptions replaceFishingParticles2(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",
            ordinal = 3), index = 0)
    private ParticleOptions replaceFishingParticles3(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",
            ordinal = 4), index = 0)
    private ParticleOptions replaceFishingParticles4(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I",
            ordinal = 5), index = 0)
    private ParticleOptions replaceFishingParticles5(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }
}