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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishingHook.class)
public class MixinFishingHook {

    @Unique FluidState fishAnywhere$fluidState;

    @Unique
    private void fishAnywhere$replaceFluidParticles(Args args){
        ParticleOptions particles = fishAnywhere$fluidState.getDripParticle();

        if(fishAnywhere$fluidState == null || particles == null){
            // Set particle count to 0
            args.set(4, 0);
        }

        if(!fishAnywhere$fluidState.is(Fluids.WATER)){
            args.set(0, particles);
        }
    }
    
    @Inject(at = @At("RETURN"), method = "calculateOpenWater", cancellable = true)
    private void calculateOpenWater(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0), method = "tick")
    private boolean noWaterCondition0(FluidState fluidState, TagKey<Fluid> fluidTagKey) {
        this.fishAnywhere$fluidState = fluidState;
        return !fluidState.isEmpty();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1), method = "tick")
    private boolean noWaterCondition1(FluidState fluidState, TagKey<Fluid> fluidTagKey) {
        return !fluidState.isEmpty();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0), method = "catchingFish")
    private boolean noWaterCondition2(BlockState blockState, Block block) {
        return !fishAnywhere$fluidState.isEmpty();
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 0), method = "catchingFish")
    private void replaceBubbleParticles0(Args args) {
        fishAnywhere$replaceFluidParticles(args);
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 1), method = "catchingFish")
    private void replaceFishingParticles0(Args args) {
        fishAnywhere$replaceFluidParticles(args);
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 2), method = "catchingFish")
    private void replaceFishingParticles1(Args args) {
        fishAnywhere$replaceFluidParticles(args);
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 3), method = "catchingFish")
    private void replaceBubbleParticles1(Args args) {
        fishAnywhere$replaceFluidParticles(args);
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 4), method = "catchingFish")
    private void replaceFishingParticles2(Args args) {
        fishAnywhere$replaceFluidParticles(args);
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", ordinal = 5), method = "catchingFish")
    private void replaceSplashParticles(Args args) {
        fishAnywhere$replaceFluidParticles(args);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"), method = "tick")
    private float offsetFluidHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return Math.max(0.1f, fluidState.getHeight(blockGetter, blockPos));
    }
}