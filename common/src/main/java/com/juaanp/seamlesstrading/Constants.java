package com.juaanp.seamlesstrading;

import net.minecraft.world.entity.npc.VillagerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "seamlesstrading";
	public static final String MOD_NAME = "SeamlessTrading";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final int DEFAULT_XP_AMOUNT = 10;
	public static final boolean DEFAULT_BOTTLES_ENABLED = true;
	public static final boolean DEFAULT_ORBS_ENABLED = true;
	public static final boolean DEFAULT_REQUIRES_CROUCHING = true;
	public static final float DEFAULT_BOTTLE_XP_MULTIPLIER = 1.0f;
	public static final float DEFAULT_ORBS_XP_MULTIPLIER = 1.0f;
	public static final float DEFAULT_ORB_ATTRACT_RANGE = 4.5f;
	public static final float DEFAULT_ORB_PICKUP_RANGE = 1.5f;

	public static final double MIN_XP_MULTIPLIER_RANGE = 0.1;
	public static final double MAX_XP_MULTIPLIER_RANGE = 5.0;

	public static final double MIN_ORB_ATTRACT_RANGE = 1.0;
	public static final double MAX_ORB_ATTRACT_RANGE = 16.0;

	public static final double MIN_ORB_PICKUP_RANGE = 0.5;
	public static final double MAX_ORB_PICKUP_RANGE = 10.0;

	// Tolerancia para comparaciones de valores de punto flotante
	public static final double FLOAT_COMPARISON_EPSILON = 0.05;

	public static final int DEFAULT_LEVELS_PER_BOTTLE = 0; // 0 = Auto, 5 = Max
	public static final int MIN_LEVELS_PER_BOTTLE = 0;
	public static final int MAX_LEVELS_PER_BOTTLE = VillagerData.MAX_VILLAGER_LEVEL - 1;

	// Default config values
	public static final boolean DEFAULT_FORCE_OPEN_WATER = false;
	public static final boolean DEFAULT_WATER_ENABLED = true;
	public static final boolean DEFAULT_LAVA_ENABLED = false;
	public static final boolean DEFAULT_EMPTY_ENABLED = false;
	public static final boolean DEFAULT_OTHER_FLUIDS_ENABLED = false;
}