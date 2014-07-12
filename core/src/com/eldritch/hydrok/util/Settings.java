package com.eldritch.hydrok.util;

public class Settings {
	// meters per pixel
	public static float SCALE = 1 / 70f;
	public static int TILE_WIDTH = 70;
	public static int TILE_HEIGHT = 70;
	
	// collision bit filters
	public static final short BIT_SOLID = 0x0002;
	public static final short BIT_LIQUID = 0x0004;
	public static final short BIT_GAS = 0x0008;
	public static final short BIT_PLASMA = 0x0010;
	public static final short ALL_BITS = BIT_SOLID | BIT_LIQUID | BIT_GAS | BIT_PLASMA;
}
