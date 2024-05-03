package com.juaanp.fish_anywhere.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public class FabricMixinFishingHook {

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

    @Inject(at = @At(value = "RETURN"), method = "calculateOpenWater", cancellable = true)
    private void forceOpenWater(BlockPos p_37159_, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(at = @At(value = "RETURN"), method = "isOpenWaterFishing", cancellable = true)
    private void forceIsOpenWaterFishing(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), method = "catchingFish")
    private BlockState forceWaterBlockState0(ServerLevel serverLevel, BlockPos blockPos) {
        return Blocks.WATER.defaultBlockState();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 1), method = "catchingFish")
    private BlockState forceWaterBlockState1(ServerLevel serverLevel, BlockPos blockPos) {
        return Blocks.WATER.defaultBlockState();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"), method = "tick")
    private float offsetFluidHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        float height = fluidState.getHeight(blockGetter, blockPos);
        return height <= 0 ? 0.1F : height;
    }
}