package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.BIT_GAS;
import static com.eldritch.hydrok.util.Settings.BIT_LIQUID;
import static com.eldritch.hydrok.util.Settings.BIT_PLASMA;
import static com.eldritch.hydrok.util.Settings.BIT_SOLID;
import static com.eldritch.hydrok.util.Settings.SCALE;
import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.level.WorldCell.Type;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class MapChunkGenerator {
    private final TextureAtlas atlas = new TextureAtlas(
            Gdx.files.internal("image-atlases/environment.atlas"));
    
    private final TiledMap[][] chunks;
    private final World world;
    private final int width;
    private final int height;

    private final LoadingCache<String, StaticTiledMapTile> tiles = CacheBuilder.newBuilder().build(
            new CacheLoader<String, StaticTiledMapTile>() {
                public StaticTiledMapTile load(String key) {
                    AtlasRegion region = atlas.findRegion(key);
                    if (region == null) {
                        return null;
                    }
                    return new StaticTiledMapTile(region);
                }
            });
    
    public MapChunkGenerator(TiledMap[][] chunks, World world, int width, int height) {
        this.chunks = chunks;
        this.world = world;
        this.width = width;
        this.height = height;
    }

    public TiledMap generate(int chunkI, int chunkJ, int worldX, int worldY) {
        TiledMap map = new TiledMap();
        map.getLayers().add(generateBackground(chunkI, chunkJ, worldX, worldY));
        return map;
    }

    private TiledMapTileLayer generateBackground(int chunkI, int chunkJ, int worldX, int worldY) {
        ChunkLayer layer = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (y == 0 && worldY == 0) {
                    // ground
                    if (hasLeft(chunkI, chunkJ)) {
                        // add variation to the terrain
                        if (Math.random() < 0.25) {
                            WorldCell cell = new WorldCell(getTile("grass/hill-right1"),
                                    worldX + x, worldY + y, world, Type.Terrain);
                            layer.setCell(x, y, cell);
                        } else {
                            WorldCell cell = new WorldCell(getTile("grass/mid"),
                                    worldX + x, worldY + y, world, Type.Terrain);
                            layer.setCell(x, y, cell);
                        }
                    } else {
                        WorldCell cell = new WorldCell(getTile("grass/mid"),
                                worldX + x, worldY + y, world, Type.Terrain);
                        layer.setCell(x, y, cell);
                    }
                } else if (worldY > 0) {
                    // sky
                    if (Math.random() < 0.025) {
                        WorldCell cell = new WorldCell(getTile("object/cloud2"),
                                worldX + x, worldY + y, world, Type.Platform);
                        layer.setCell(x, y, cell);
                    }
                } else {
                    // underground
                }
            }
        }
        return layer;
    }

    private boolean hasLeft(int i, int j) {
        int left = j - 1;
        return left >= 0 && chunks[i][left] != null;
    }
    
    private TiledMap getLeft(int i, int j) {
        return chunks[i][j - 1];
    }
    
    private boolean hasUp(int i, int j) {
        int up = j + 1;
        return up < chunks.length && chunks[up][j] != null;
    }
    
    private TiledMap getUp(int i, int j) {
        return chunks[i + 1][j];
    }
    
    private boolean hasDown(int i, int j) {
        int down = j - 1;
        return down >= 0 && chunks[down][j] != null;
    }
    
    private TiledMap getDown(int i, int j) {
        return chunks[i - 1][j];
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
