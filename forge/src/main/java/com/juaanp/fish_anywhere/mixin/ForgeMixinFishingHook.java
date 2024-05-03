package com.juaanp.fish_anywhere.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public class ForgeMixinFishingHook {

    @Unique
    private FluidState fishAnywhere$fluidState;

    @Unique
    private ParticleOptions fishAnywhere$replaceParticles(ParticleOptions defaultParticles){
        ParticleOptions particles = fishAnywhere$fluidState.getDripParticle();

        if(particles != null && !fishAnywhere$fluidState.is(Fluids.WATER)){
            return particles;
        }

        return defaultParticles;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 0), index = 0 , method = "catchingFish")
    private ParticleOptions replaceBubbleParticles0(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 1), index = 0 , method = "catchingFish")
    private ParticleOptions replaceFishingParticles0(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 2), index = 0 , method = "catchingFish")
    private ParticleOptions replaceFishingParticles1(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 3), index = 0 , method = "catchingFish")
    private ParticleOptions replaceBubbleParticles1(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 4), index = 0 , method = "catchingFish")
    private ParticleOptions replaceFishingParticles2(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 5), index = 0 , method = "catchingFish")
    private ParticleOptions replaceSplashParticles(ParticleOptions particleOptions) {
        return fishAnywhere$replaceParticles(particleOptions);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0), method = "tick")
    private boolean noWaterCondition0(FluidState fluidState, TagKey<Fluid> tagKey) {
        this.fishAnywhere$fluidState = fluidState;
        return !fluidState.isEmpty();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1), method = "tick")
    private boolean noWaterCondition1(FluidState fluidState, TagKey<Fluid> tagKey) {
        return !fluidState.isEmpty();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;calculateOpenWater(Lnet/minecraft/core/BlockPos;)Z"), method = "tick")
    private boolean forceOpenWater(FishingHook fishingHook, BlockPos blockPos) {
        return true;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;openWater:Z"), method = "isOpenWaterFishing")
    private boolean forceIsOpenWaterFishing(FishingHook fishingHook) {
        return true;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getMaterial()Lnet/minecraft/world/level/material/Material;", ordinal = 0), method = "catchingFish")
    private Material forceWaterMaterial0(BlockState blockState) {
        return Material.WATER;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getMaterial()Lnet/minecraft/world/level/material/Material;", ordinal = 1), method = "catchingFish")
    private Material forceWaterMaterial1(BlockState blockState) {
        return Material.WATER;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"), method = "tick")
    private float offsetFluidHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        float height = fluidState.getHeight(blockGetter, blockPos);
        return height <= 0 ? 0.1f : height;
    }
}