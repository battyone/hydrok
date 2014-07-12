package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.SCALE;
import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.util.Settings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class MapChunkGenerator {
	private final TextureAtlas atlas = new TextureAtlas(
			Gdx.files.internal("image-atlases/environment.atlas"));
	
	private final LoadingCache<String, StaticTiledMapTile> tiles = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, StaticTiledMapTile>() {
				public StaticTiledMapTile load(String key) {
					AtlasRegion region = atlas.findRegion(key);
					if (region == null) {
						return null;
					}
					return new StaticTiledMapTile(region);
				}
			});
	
	private final LoadingCache<String, MultiTileStatic> multiTiles = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, MultiTileStatic>() {
				public MultiTileStatic load(String key) {
					AtlasRegion region = atlas.findRegion(key);
					if (region == null) {
						return null;
					}
					return new MultiTileStatic(region);
				}
			});

	public TiledMap generate(World world, int worldX, int worldY, int width, int height) {
		TiledMap map = new TiledMap();
		map.getLayers().add(generateBackground(world, worldX, worldY, width, height));
		return map;
	}

	private TiledMapTileLayer generateBackground(World world, int worldX, int worldY,
			int width, int height) {
		
		ChunkLayer layer = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT);
		for (int x = 0; x < layer.getWidth(); x++) {
			for (int y = 0; y < layer.getHeight(); y++) {
				if (y == 0 && worldY == 0) {
					// ground
					layer.addCell(getTile("grass/mid"), x, y);
					layer.addBody(createBox(world, worldX + x, worldY + y));
				} else if (worldY > 0) {
					// sky
					if (Math.random() < 0.025) {
						StaticTiledMapTile tile = getTile("object/cloud2");
						layer.addCell(tile, x, y);
						layer.addBody(createBox(world, worldX + x, worldY + y, tile));
					}
				} else {
					// underground
				}
			}
		}
		return layer;
	}
	
	private Body createBox(World world, int x, int y) {
		return createBox(world, x, y, 0.5f, 0.5f);
	}
	
	private Body createBox(World world, int x, int y, StaticTiledMapTile tile) {
		float halfWidth = (tile.getTextureRegion().getRegionWidth() / 2.0f) * SCALE;
		float halfHeight = (tile.getTextureRegion().getRegionHeight() / 2.0f) * SCALE;
		return createBox(world, x, y, halfWidth, halfHeight);
	}
	
	private Body createBox(World world, int x, int y, float halfWidth, float halfHeight) {
		// Create our body definition
		BodyDef groundBodyDef = new BodyDef();
		// Set its world position
		groundBodyDef.position.set(new Vector2(x + halfWidth, y + halfHeight));

		// Create a body from the defintion and add it to the world
		Body groundBody = world.createBody(groundBodyDef);

		// Create a polygon shape
		PolygonShape groundBox = new PolygonShape();
		
		// setAsBox takes half-width and half-height as arguments
		groundBox.setAsBox(halfWidth, halfHeight);
		
		// Create a fixture from our polygon shape and add it to our ground body
		groundBody.createFixture(groundBox, 0.0f);
		
		// Clean up after ourselves
		groundBox.dispose();
		
		return groundBody;
	}
	
	private StaticTiledMapTile getTile(String key) {
		try {
			return tiles.get(key);
		} catch (ExecutionException ex) {
			HydrokGame.error("Failed loading tile: " + key, ex);
			return null;
		}
	}
	
	private MultiTileStatic getMultiTile(String key) {
		try {
			return multiTiles.get(key);
		} catch (ExecutionException ex) {
			HydrokGame.error("Failed loading multi-tile: " + key, ex);
			return null;
		}
	}
	
	private static class MultiTileStatic {
        private final TiledMapTile[][] tiles;

        public MultiTileStatic(TextureRegion texture) {
            TextureRegion[][] grid = texture.split(Settings.TILE_WIDTH, Settings.TILE_HEIGHT);
            tiles = new TiledMapTile[grid.length][];
            for (int i = 0; i < grid.length; i++) {
                tiles[i] = new TiledMapTile[grid[i].length];
                for (int j = 0; j < grid[i].length; j++) {
                    tiles[i][j] = new StaticTiledMapTile(grid[i][j]);
                }
            }
        }

        public void addTo(ChunkLayer layer, int x, int y) {
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[i].length; j++) {
                    layer.addCell(tiles[i][j], x + j, y - i);
                }
            }
        }
    }
}
