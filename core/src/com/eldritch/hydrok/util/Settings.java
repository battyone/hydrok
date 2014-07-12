package com.eldritch.hydrok.util;

public class Settings {
	// meters per pixel
	public static float SCALE = 1 / 70f;
	
	public static int TILE_WIDTH = 70;
	
	public static int TILE_HEIGHT = 70;
	
	// collision bit filters
	public static final short BIT_SOLID = 0x0001;
	public static final short BIT_LIQUID = 0x0010;
	public static final short BIT_GAS = 0x0100;
	public static final short BIT_PLASMA = 0x1000;
}
