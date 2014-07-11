package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.eldritch.hydrok.HydrokGame;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class MapChunkGenerator {
	private final TextureAtlas atlas = new TextureAtlas(
			Gdx.files.internal("image-atlases/environment.atlas"));
	
	private final LoadingCache<String, StaticTiledMapTile> tiles = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, StaticTiledMapTile>() {
				public StaticTiledMapTile load(String key) {
					return new StaticTiledMapTile(atlas.findRegion(key));
				}
			});

	public TiledMap generate(int width, int height) {
		TiledMap map = new TiledMap();
		map.getLayers().add(generateBackground(width, height));
		return map;
	}

	private TiledMapTileLayer generateBackground(int width, int height) {
		TiledMapTileLayer layer = new TiledMapTileLayer(width, height,
				TILE_WIDTH, TILE_HEIGHT);
		for (int x = 0; x < layer.getWidth(); x++) {
			for (int y = 0; y < layer.getHeight(); y++) {
				if (y == 0) {
					Cell cell = new Cell();
					cell.setTile(getTile("grass/mid"));
					layer.setCell(x, y, cell);
				}
			}
		}
		return layer;
	}
	
	private StaticTiledMapTile getTile(String key) {
		try {
			return tiles.get(key);
		} catch (ExecutionException ex) {
			HydrokGame.error("Failed loading tile: " + key, ex);
			return null;
		}
	}
}
