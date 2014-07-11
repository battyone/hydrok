package com.eldritch.hydrok.level;

import com.badlogic.gdx.maps.tiled.TiledMap;

public class ProceduralTiledMap extends TiledMap {
	private final TiledMap[][] chunks = new TiledMap[3][3];
	private final MapChunkGenerator generator = new MapChunkGenerator();
	private final int width;
	private final int height;
	
	public ProceduralTiledMap(int width, int height) {
		this.width = width;
		this.height = height;
		for (int i = 0; i < chunks.length; i++) {
			for (int j = 0; j < chunks[i].length; j++) {
				chunks[i][j] = generator.generate(width, height);
			}
		}
	}
}
