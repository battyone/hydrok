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
import com.eldritch.hydrok.activator.PhaseActivator;
import com.eldritch.hydrok.activator.PhaseActivator.Fireball;
import com.eldritch.hydrok.activator.PhaseActivator.IceShard;
import com.eldritch.hydrok.activator.PhaseActivator.WaterDroplet;
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
    final Array<WorldCell> terrainCells = new Array<WorldCell>();
    private WorldCell lastTerrain = null;
    private Body chainBody = null;

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
    
    public void removeVertices(int minRemaining) {
        if (terrainCells.size <= minRemaining) {
            return;
        }
        int count = terrainCells.size - minRemaining;
        terrainCells.removeRange(0, count - 1);
    }

    public TiledMap generate(int chunkI, int chunkJ, int worldX, int worldY) {
        TiledMap map = new TiledMap();
        ChunkLayer background = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT);
        generateTerrain(background, chunkI, chunkJ, worldX, worldY);
        generateObstacles(background, chunkI, chunkJ, worldX, worldY);
        generateBackground(background, chunkI, chunkJ, worldX, worldY);
        genrateActivators(background, chunkI, chunkJ, worldX, worldY);
        map.getLayers().add(background);
        return map;
    }
    
    private void genrateActivators(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (layer.getCell(x, y) != null) {
                    // already has cell
                    continue;
                }
                
                WorldCell down = getCell(layer, x, y - 1, chunkI, chunkJ, 0);
                if (down != null && down.getType() == Type.Terrain) {
                    if (Math.random() < 0.1) {
                        PhaseActivator a = new Fireball(x + worldX, y + worldY, world);
                        WorldCell cell = new WorldCell(a.getTile(), x, y, a.getX(), a.getY(), world, Type.Activator);
                        layer.setCell(x, y, cell);
                        layer.addBody(a.getBody());
                    }
                } else if (down != null && down.getType() == Type.Platform) {
                    if (Math.random() < 0.1) {
                        PhaseActivator a = new WaterDroplet(x + worldX, y + worldY, world);
                        WorldCell cell = new WorldCell(a.getTile(), x, y, a.getX(), a.getY(), world, Type.Activator);
                        layer.setCell(x, y, cell);
                        layer.addBody(a.getBody());
                    }
                } else if (down == null) {
                    if (Math.random() < 0.025) {
                        PhaseActivator a = new IceShard(x + worldX, y + worldY, world);
                        WorldCell cell = new WorldCell(a.getTile(), x, y, a.getX(), a.getY(), world, Type.Activator);
                        layer.setCell(x, y, cell);
                        layer.addBody(a.getBody());
                    }
                }
            }
        }
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
                        WorldCell cell = new WorldCell(getTile("object/cloud2"), x, y, worldX + x, worldY
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
                        WorldCell cell = new WorldCell(tile, x, y, worldX + x, worldY + y,
                                world, Type.Filler);
                        layer.setCell(x, y, cell);
                    }
                }
            }
        }
    }
    
    private boolean outsideLayer(WorldCell lastTerrain, ChunkLayer layer, int worldY) {
        if (lastTerrain == null) {
            return true;
        }
        
        // allowed to be within, just below, or just above
        int localY = lastTerrain.getWorldY() - worldY;
        return localY < -1 || localY > layer.getHeight();
    }
    
    private void generateTerrain(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        int vertexCount = 0;
        if (lastTerrain == null) {
            if (worldX == 0 && worldY == 0) {
                // seed the first cell
                lastTerrain = new WorldCell(getTile("grass/mid"), 0, 0, 0, 0, world, Type.Terrain);
                layer.setCell(0, 0, lastTerrain);
                terrainCells.add(lastTerrain);
                vertexCount++;
            }
        }
        
        if (outsideLayer(lastTerrain, layer, worldY)) {
            // wrong layer for terrain
            return;
        }
        
        // check to see if this layer needs to be reconstructed from existing terrain
        if (lastTerrain.getWorldX() - worldX + 1 >= layer.getWidth()) {
            regenerateTerrain(layer, chunkI, chunkJ, worldX, worldY);
            return;
        }
        
        for (int x2 = lastTerrain.getWorldX() - worldX + 1; x2 < layer.getWidth(); x2++) {
            int y = lastTerrain.getWorldY() - worldY;
            
            Array<WorldCell> candidates = new Array<WorldCell>();
            int[] offsets = { -1, 1, 0 };
            for (int dy : offsets) {
                int y2 = y + dy;
                if (y2 < 0 || y2 >= layer.getHeight()) {
                    // y-coordinate out of bounds
                    continue;
                }
                
                // add variation to the terrain
                if (lastTerrain.matchesSlope(-1, worldY + y2)) {
                    candidates.add(new WorldCell(getTile("grass/hill-right1"), x2, y2, worldX + x2,
                            worldY + y2, world, Type.Terrain, -1));
                } 
                if (lastTerrain.matchesSlope(1, worldY + y2)) {
                    candidates.add(new WorldCell(getTile("grass/hill-left1"), x2, y2, worldX + x2,
                            worldY + y2, world, Type.Terrain, 1));
                } 
                if (lastTerrain.matchesSlope(0, worldY + y2)) {
                    candidates.add(new WorldCell(getTile("grass/mid"), x2, y2, worldX + x2,
                            worldY + y2, world, Type.Terrain));
                }
            }
            
            if (candidates.size > 0) {
                WorldCell cell = candidates.get((int) (Math.random() * candidates.size));
                layer.setCell(cell.getLocalX(), cell.getLocalY(), cell);
                terrainCells.add(cell);
                vertexCount++;
                
                lastTerrain = cell;
            }
        }
        
        // check for terrain vertices
        if (vertexCount >= 2) {
            // create the body
            BodyDef bdef = new BodyDef();
            bdef.type = BodyType.StaticBody;
            
            ChainShape chain = new ChainShape();
            Vector2[] vertices = new Vector2[terrainCells.size];
            for (int i = 0; i < terrainCells.size; i++) {
                vertices[i] = terrainCells.get(i).getTerrainVector();
            }
            chain.createChain(vertices);
            
            FixtureDef fd = new FixtureDef();
            fd.shape = chain;
            fd.filter.categoryBits = 0x0001;
            fd.filter.maskBits = Type.Terrain.getMaskBits();
            
            if (chainBody != null) {
                world.destroyBody(chainBody);
            }
            chainBody = world.createBody(bdef);
            chainBody.createFixture(fd);
            chain.dispose();
        }
    }
    
    private void regenerateTerrain(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (WorldCell cell : terrainCells) {
            int x = cell.getWorldX() - worldX;
            if (x < 0 || x >= layer.getHeight()) {
                // x-coordinate out of bounds
                continue;
            }
            
            int y = cell.getWorldY() - worldY;
            if (y < 0 || y >= layer.getHeight()) {
                // y-coordinate out of bounds
                continue;
            }
            
            layer.setCell(cell.getLocalX(), cell.getLocalY(), cell);
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
