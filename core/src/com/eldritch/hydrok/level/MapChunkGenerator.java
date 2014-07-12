package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.level.ProceduralTiledMap.L;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;
import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;

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
    Array<Vector2> vertices = new Array<Vector2>();

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
        map.getLayers().add(generateBackground(chunkI, chunkJ, worldX, worldY));
        return map;
    }

    private TiledMapTileLayer generateBackground(int chunkI, int chunkJ, int worldX, int worldY) {
        int vertexCount = 0;
        ChunkLayer layer = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (x == 0 && worldX == 0 && y == 0 && worldY == 0) {
                    // seed the first cell
                    WorldCell cell = new WorldCell(getTile("grass/mid"), worldX + x, worldY + y,
                            world, Type.Terrain);
                    layer.setCell(x, y, cell);
                    vertices.add(new Vector2(x + worldX, y + worldY + 1));
                    vertexCount++;
                }
                
                WorldCell left = getLeft(layer, x, y, chunkI, chunkJ, 0);
                if (left != null) {
                    // add variation to the terrain
                    WorldCell cell;
                    if (Math.random() < 0.25 && left.getSlope() < 1) {
                        cell = new WorldCell(getTile("grass/hill-right1"), worldX + x,
                                worldY + y, world, Type.Terrain, -1);
//                    } else if (Math.random() < 0.25 && left.getSlope() > -1) {
//                        cell = new WorldCell(getTile("grass/hill-left1"), worldX + x,
//                                worldY + y, world, Type.Terrain, 1);
                    } else {
                        cell = new WorldCell(getTile("grass/mid"), worldX + x,
                                worldY + y, world, Type.Terrain);
                    }
                    layer.setCell(x, y, cell);
                    left.setNext(cell);
                    vertices.add(new Vector2(x + worldX, y + worldY + 1));
                    vertexCount++;
                } else if (worldY > 0) {
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
        
        return layer;
    }

    private WorldCell getLeft(ChunkLayer layer, int x, int y, int chunkI, int chunkJ, int z) {
        // begin checking all immediately left cells for a terrain piece
        for (int dy = -1; dy <= 1; dy++) {
            // check tile contents
            int y2 = y + dy;
            WorldCell cell = getCell(layer, x - 1, y2, chunkI, chunkJ, z);
            if (cell != null && !cell.hasNext() &&
                    cell.getType() == Type.Terrain && cell.matchesSlope(dy)) {
                // found a valid terrain type
                return cell;
            }
        }

        // nothing valid found
        return null;
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
