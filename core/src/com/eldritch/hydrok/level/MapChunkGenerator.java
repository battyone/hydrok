package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.level.ProceduralTiledMap.L;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;
import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;

import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
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
    final Array<Vector2> vertices = new Array<Vector2>();
    private WorldCell lastTerrain = null;

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
    
    public void removeVertices(int count) {
        if (count > 0) {
            vertices.removeRange(0, count - 1);
        }
    }

    public TiledMap generate(int chunkI, int chunkJ, int worldX, int worldY) {
        TiledMap map = new TiledMap();
        ChunkLayer background = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT);
        generateTerrain(background, chunkI, chunkJ, worldX, worldY);
        generateObstacles(background, chunkI, chunkJ, worldX, worldY);
        generateBackground(background, chunkI, chunkJ, worldX, worldY);
        map.getLayers().add(background);
        return map;
    }
    
    private void generateObstacles(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (layer.getCell(x, y) != null) {
                    // already has cell
                    continue;
                }
                
                if (worldY > 0) {
                    // sky
                    if (Math.random() < 0.025) {
                        WorldCell cell = new WorldCell(getTile("object/cloud2"), worldX + x, worldY
                                + y, world, Type.Platform);
                        layer.setCell(x, y, cell);
                    }
                } else {
                    // underground
                }
            }
        }
    }
    
    private void generateBackground(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            // top -> bottom
            for (int y = layer.getHeight() - 1; y >= 0; y--) {
                if (layer.getCell(x, y) != null) {
                    // already has cell
                    continue;
                }
                
                WorldCell up = getCell(layer, x, y + 1, chunkI, chunkJ, 0);
                if (up != null) {
                    if (up.getType() == Type.Terrain || up.getType() == Type.Filler) {
                        StaticTiledMapTile tile = getTile("grass/center");
                        if (up.getSlope() < 0) {
                            tile = getTile("grass/hill-right2");
                        } else if (up.getSlope() > 0) {
                            tile = getTile("grass/hill-left2");
                        }
                        WorldCell cell = new WorldCell(tile, worldX + x, worldY + y,
                                world, Type.Filler);
                        layer.setCell(x, y, cell);
                    }
                }
            }
        }
    }
    
    private void generateTerrain(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        int vertexCount = 0;
        if (lastTerrain == null && worldX == 0 && worldY == 0) {
            // seed the first cell
            lastTerrain = new WorldCell(getTile("grass/mid"), 0, 0, world, Type.Terrain);
            layer.setCell(0, 0, lastTerrain);
            vertices.add(new Vector2(0, 1));
            vertexCount++;
        }
        
        // add terrain until no longer able to
        boolean canContinue = lastTerrain != null;
        while (canContinue) {
            int x = lastTerrain.getX() - worldX;
            int y = lastTerrain.getY() - worldY;
            
            int x2 = x + 1;
            if (x2 < 0 || x2 >= layer.getWidth()) {
                // x-coordinate out of bounds
                canContinue = false;
                break;
            }
            
            int[] offsets = { -1, 1, 0 };
            canContinue = false;
            for (int dy : offsets) {
                int y2 = y + dy;
                if (y2 < 0 || y2 >= layer.getHeight()) {
                    // y-coordinate out of bounds
                    continue;
                }
                
                // add variation to the terrain
                WorldCell cell;
                int vy = 1;
                if (Math.random() < 0.25 && lastTerrain.matchesSlope(-1, worldY + y2)) {
                    cell = new WorldCell(getTile("grass/hill-right1"), worldX + x2,
                            worldY + y2, world, Type.Terrain, -1);
                    
                } else if (Math.random() < 0.25 && lastTerrain.matchesSlope(1, worldY + y2)) {
                    cell = new WorldCell(getTile("grass/hill-left1"), worldX + x2,
                            worldY + y2, world, Type.Terrain, 1);
                    vy = 0;
                } else if (lastTerrain.matchesSlope(0, worldY + y2)) {
                    cell = new WorldCell(getTile("grass/mid"), worldX + x2,
                            worldY + y2, world, Type.Terrain);
                } else {
                    cell = null;
                }
                
                // add the cell if it was set
                if (cell != null) {
                    layer.setCell(x2, y2, cell);
                    lastTerrain.setNext(cell);
                    vertices.add(new Vector2(x2 + worldX, y2 + worldY + vy));
                    vertexCount++;
                    
                    lastTerrain = cell;
                    canContinue = true;
                    break;
                }
            }
        }
        
        // check for terrain
        if (vertexCount >= 2) {
            // create the body
            BodyDef bdef = new BodyDef();
            bdef.type = BodyType.StaticBody;
            
            ChainShape chain = new ChainShape();
            chain.createChain((Vector2[]) vertices.toArray(Vector2.class));
            
            FixtureDef fd = new FixtureDef();
            fd.shape = chain;
            fd.filter.categoryBits = 0x0001;
            fd.filter.maskBits = Type.Terrain.getMaskBits();
            
            Body body = world.createBody(bdef);
            body.createFixture(fd);
            chain.dispose();
            
            layer.addBody(body);
            layer.setVertexCount(vertexCount);
        }
    }

    private WorldCell getCell(ChunkLayer layer, int x, int y, int chunkI, int chunkJ, int z) {
        // get the updated chunk
        int chunkX = (int) Math.floor(1.0 * x / width) + chunkJ;
        int chunkY = (int) Math.floor(1.0 * y / height) + chunkI;

        // handle out of bounds
        if (chunkX < 0 || chunkY < 0 || chunkX >= L || chunkY >= L) {
            return null;
        }

        // update layer if chunks differ
        if (chunkX != chunkJ || chunkY != chunkI) {
            if (chunks[chunkY][chunkX] == null
                    || chunks[chunkY][chunkX].getLayers().getCount() <= z) {
                // out of bounds
                return null;
            }
            layer = (ChunkLayer) chunks[chunkY][chunkX].getLayers().get(z);
        }

        // return the cell within chunk
        int tileX = x - (chunkX - chunkJ) * width;
        int tileY = y - (chunkY - chunkI) * height;
        return layer.getCell(tileX, tileY);
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
