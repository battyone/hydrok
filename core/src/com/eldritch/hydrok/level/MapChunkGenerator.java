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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
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

	public TiledMap generate(World world, int worldX, int worldY, int width, int height) {
		TiledMap map = new TiledMap();
		map.getLayers().add(generateBackground(world, worldX, worldY, width, height));
		return map;
	}

	private TiledMapTileLayer generateBackground(World world, int worldX, int worldY,
			int width, int height) {
		
		TiledMapTileLayer layer = new TiledMapTileLayer(width, height,
				TILE_WIDTH, TILE_HEIGHT);
		for (int x = 0; x < layer.getWidth(); x++) {
			for (int y = 0; y < layer.getHeight(); y++) {
				if (y == 0) {
					Cell cell = new Cell();
					cell.setTile(getTile("grass/mid"));
					layer.setCell(x, y, cell);
					createBox(worldX + x, worldY + y, world);
				}
			}
		}
		return layer;
	}
	
	private void createBox(int x, int y, World world) {
		// Create our body definition
		BodyDef groundBodyDef = new BodyDef();
		// Set its world position
		groundBodyDef.position.set(new Vector2(x + 0.5f, y + 0.5f));

		// Create a body from the defintion and add it to the world
		Body groundBody = world.createBody(groundBodyDef);

		// Create a polygon shape
		PolygonShape groundBox = new PolygonShape();
		
		// setAsBox takes half-width and half-height as arguments
		groundBox.setAsBox(0.5f, 0.5f);
		
		// Create a fixture from our polygon shape and add it to our ground body
		groundBody.createFixture(groundBox, 0.0f);
		
		// Clean up after ourselves
		groundBox.dispose();
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
