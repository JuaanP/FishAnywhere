package com.juaanp.fishanywhere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "fishanywhere";
	public static final String MOD_NAME = "FishAnywhere";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final double FLOAT_COMPARISON_EPSILON = 0.05;

	public static final boolean DEFAULT_FORCE_OPEN_WATER = true;

	public static final int CONFIG_VERSION = 1;
	public static final String CONFIG_FILENAME = MOD_ID + ".json";
	public static final String CONFIG_BACKUP_DIR = MOD_ID + "_backups";
}