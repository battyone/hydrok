package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player;

public class ProceduralTiledMap extends TiledMap {
	private static final int L = 3;
	
	private final TiledMap[][] chunks = new TiledMap[L][L];
	private final MapChunkGenerator generator = new MapChunkGenerator();
	private final World world;
	private final int chunkWidth;
	private final int chunkHeight;
	private final Vector2 lastPosition = new Vector2();
	
	private int minX = 0;
	private int minY = 0;
	
	public ProceduralTiledMap(World world, int width, int height) {
		this.world = world;
		this.chunkWidth = width;
		this.chunkHeight = height;
		
		// generate initial chunk setup
		for (int i = 0; i < chunks.length; i++) {
			for (int j = 0; j < chunks[i].length; j++) {
				chunks[i][j] = generator.generate(
						world, j * chunkWidth, i * chunkHeight, chunkWidth, chunkHeight);
			}
		}
		
		// add index layers
		getLayers().add(new ProceduralLayer(0, width * L, height * L, TILE_WIDTH, TILE_HEIGHT));
	}
	
	public void update(Player player) {
		// compare last chunk with the current chunk
		int lastX = (int) (lastPosition.x / chunkWidth);
		int lastY = (int) (lastPosition.y / chunkHeight);
		
		Vector2 position = player.getPosition();
		int chunkX = (int) (position.x / chunkWidth);
		int chunkY = (int) (position.y / chunkHeight);
		
		// check for horizontal crossing
		if (lastX < chunkX) {
			// right
			for (int i = 0; i < chunks.length; i++) {
				for (int j = 0; j < chunks[i].length - 1; j++) {
					// shift left
					chunks[i][j] = chunks[i][j + 1];
				}
				
				// regen last column
				int j = chunks.length - 1;
				chunks[i][j] = generator.generate(
						world, (chunkX + j) * chunkWidth, (chunkY + i) * chunkHeight,
						chunkWidth, chunkHeight);
			}
			
			// reset min x position
			minX = chunkX * chunkWidth;
		}
		
		// check for vertical crossing
		if (lastY < chunkY) {
			// up
		} else if (lastY > chunkY) {
			// down
		}
		
		// reset the last position
		lastPosition.set(position);
	}
	
	private class ProceduralLayer extends TiledMapTileLayer {
		private final int index;
		
		public ProceduralLayer(int index, int width, int height, int tileWidth, int tileHeight) {
			super(width, height, tileWidth, tileHeight);
			this.index = index;
		}
		
		@Override
		public Cell getCell(int x, int y) {
			// adjust for shifting
			x -= minX;
			y -= minY;
			
			// get relevant chunk
			int chunkX = x / chunkWidth;
			int chunkY = y / chunkHeight;
			
			// handle out of bounds
			if (chunkX < 0 || chunkY < 0 || chunkX >= L || chunkY >= L) {
				return null;
			}
			
			// return the cell within chunk
			TiledMapTileLayer layer = (TiledMapTileLayer) 
					chunks[chunkY][chunkX].getLayers().get(index);
			int tileX = x - chunkX * chunkWidth;
			int tileY = y - chunkY * chunkHeight;
			return layer.getCell(tileX, tileY);
		}
	}
}
