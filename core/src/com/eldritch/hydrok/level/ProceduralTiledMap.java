package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class ProceduralTiledMap extends TiledMap {
	private static final int L = 3;
	
	private final TiledMap[][] chunks = new TiledMap[L][L];
	private final MapChunkGenerator generator = new MapChunkGenerator();
	private final int chunkWidth;
	private final int chunkHeight;
	
	public ProceduralTiledMap(int width, int height) {
		this.chunkWidth = width;
		this.chunkHeight = height;
		
		// generate initial chunk setup
		for (int i = 0; i < chunks.length; i++) {
			for (int j = 0; j < chunks[i].length; j++) {
				chunks[i][j] = generator.generate(width, height);
			}
		}
		
		// add index layers
		getLayers().add(new ProceduralLayer(0, width * L, height * L, TILE_WIDTH, TILE_HEIGHT));
	}
	
	private class ProceduralLayer extends TiledMapTileLayer {
		private final int index;
		
		public ProceduralLayer(int index, int width, int height, int tileWidth, int tileHeight) {
			super(width, height, tileWidth, tileHeight);
			this.index = index;
		}
		
		@Override
		public Cell getCell(int x, int y) {
			// get relevant chunk
			int chunkX = x / chunkWidth;
			int chunkY = y / chunkHeight;
			
			// handle out of bounds
			if (chunkX < 0 || chunkY < 0 || chunkX >= L || chunkY >= L) {
				return null;
			}
			
			// return the cell within chunk
			TiledMapTileLayer layer = (TiledMapTileLayer) 
					chunks[chunkX][chunkY].getLayers().get(index);
			int tileX = x - chunkX * chunkWidth;
			int tileY = y - chunkY * chunkHeight;
			return layer.getCell(tileX, tileY);
		}
	}
}
