package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import com.badlogic.gdx.maps.MapLayer;
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
		
		// generate initial chunk setup: [0, 0] is bottom left
		for (int i = 0; i < chunks.length; i++) {
			for (int j = 0; j < chunks[i].length; j++) {
			    chunks[i][j] = generate(i, j, 0, 0);
			}
		}
		
		// add index layers
		getLayers().add(new ProceduralLayer(0, width * L, height * L, TILE_WIDTH, TILE_HEIGHT));
	}
	
	private TiledMap generate(int i, int j, int chunkX, int chunkY) {
	    return generator.generate(
	            chunks,
	            i,
	            j,
                world,
                (chunkX + j) * chunkWidth - chunkWidth,
                (chunkY + i) * chunkHeight - chunkHeight,
                chunkWidth,
                chunkHeight);
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
				// destroy the first column
				for (MapLayer layer : chunks[i][0].getLayers()) {
					((ChunkLayer) layer).destroy();
				}
				
				for (int j = 0; j < chunks[i].length - 1; j++) {
					// shift left
					chunks[i][j] = chunks[i][j + 1];
				}
				
				// regen last column
				int j = chunks.length - 1;
				chunks[i][j] = generate(i, j, chunkX, chunkY);
			}
			
			// reset min x position
			minX = chunkX * chunkWidth;
		}
		
		// check for vertical crossing
		if (lastY < chunkY) {
			// up
			for (int j = 0; j < chunks.length; j++) {
				// destroy the first row
				for (MapLayer layer : chunks[0][j].getLayers()) {
					((ChunkLayer) layer).destroy();
				}
				
				for (int i = 0; i < chunks.length - 1; i++) {
					// shift down
					chunks[i][j] = chunks[i + 1][j];
				}
				
				// regen last row
				int i = chunks.length - 1;
				chunks[i][j] = generate(i, j, chunkX, chunkY);
			}
			
			// reset min y position
			minY = chunkY * chunkHeight;
		} else if (lastY > chunkY) {
			// down
			for (int j = 0; j < chunks.length; j++) {
				// destroy the last row
				for (MapLayer layer : chunks[chunks.length - 1][j].getLayers()) {
					((ChunkLayer) layer).destroy();
				}
				
				for (int i = chunks.length - 1; i >= 1; i--) {
					// shift up
					chunks[i][j] = chunks[i - 1][j];
				}
				
				// regen first row
				int i = 0;
				chunks[i][j] = generate(i, j, chunkX, chunkY);
			}
			
			// reset min y position
			minY = chunkY * chunkHeight;
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
			
			x += chunkWidth;
			y += chunkHeight;
			
			// get relevant chunk
			int chunkX = x / chunkWidth;
			int chunkY = y / chunkHeight;
			
			// handle out of bounds
			if (chunkX < 0 || chunkY < 0 || chunkX >= L || chunkY >= L) {
				return null;
			}
			
			// check for layer existence
			if (chunks[chunkY][chunkX].getLayers().getCount() <= index) {
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
