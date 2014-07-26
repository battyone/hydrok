package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.BIT_LIQUID;
import static com.eldritch.hydrok.util.Settings.BIT_SOLID;
import static com.eldritch.hydrok.util.Settings.CHUNKS;
import static com.eldritch.hydrok.util.Settings.SCALE;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;
import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.activator.PhaseActivator;
import com.eldritch.hydrok.activator.TiledPhaseActivator.GasActivator;
import com.eldritch.hydrok.activator.TiledPhaseActivator.LiquidActivator;
import com.eldritch.hydrok.activator.TiledPhaseActivator.PlasmaActivator;
import com.eldritch.hydrok.activator.TiledPhaseActivator.SolidActivator;
import com.eldritch.hydrok.entity.Barnacle;
import com.eldritch.hydrok.entity.Blower;
import com.eldritch.hydrok.entity.Fly;
import com.eldritch.hydrok.entity.Entity;
import com.eldritch.hydrok.level.WorldCell.Type;
import com.eldritch.hydrok.util.HydrokContactListener;
import com.eldritch.hydrok.util.Settings;
import com.eldritch.hydrok.util.TilePoint;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class MapChunkGenerator {
    private final TextureAtlas atlas = new TextureAtlas(
            Gdx.files.internal("image-atlases/environment.atlas"));

    private final Randomizer rand = new Randomizer();
    private final List<Entity> newEntities = new ArrayList<Entity>();
    private final TiledMap[][] chunks;
    private final HydrokContactListener contactListener;
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

    public MapChunkGenerator(HydrokContactListener contactListener, TiledMap[][] chunks, World world, int width, int height) {
        this.chunks = chunks;
        this.contactListener = contactListener;
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
    
    public List<Entity> getNewEntities() {
        return newEntities;
    }

    public TiledMap generate(int chunkI, int chunkJ, int worldX, int worldY) {
        rand.update(worldX);
        
        TiledMap map = new TiledMap();
        ChunkLayer background = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT, 0);
        ChunkLayer terrain = new ChunkLayer(world, width, height, TILE_WIDTH, TILE_HEIGHT, 1);

        generateTerrain(terrain, chunkI, chunkJ, worldX, worldY);
        generateBackground(terrain, chunkI, chunkJ, worldX, worldY);
        
        if (worldX > Settings.CHUNK_WIDTH * 2) {
            generateWater(background, terrain, chunkI, chunkJ, worldX, worldY);
            generateObstacles(terrain, chunkI, chunkJ, worldX, worldY);
            generateActivators(terrain, chunkI, chunkJ, worldX, worldY);
            generateEntities(terrain, chunkI, chunkJ, worldX, worldY);
        }

        map.getLayers().add(background);
        map.getLayers().add(terrain);

        return map;
    }
    
    private void generateEntities(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (!isNullOrEmpty(layer.getCell(x, y))) {
                    // already has cell
                    continue;
                }
                
                WorldCell down = getCell(layer, x, y - 1, chunkI, chunkJ);
                WorldCell left = getCell(layer, x - 1, y - 1, chunkI, chunkJ);
                WorldCell right = getCell(layer, x + 1, y - 1, chunkI, chunkJ);
                
                if (rand.flip(0.025)) {
                    // fly
                    newEntities.add(new Fly(x + worldX, y + worldY, world));
                } else if (y > layer.getTerrainLimit() && rand.flip(0.01)) {
                    // blower
                    newEntities.add(new Blower(x + worldX, y + worldY, world));
                } else if (isPlatform(down) && isPlatform(left) && isPlatform(right) && rand.flip(0.5)) {
                    // barnacle
                    newEntities.add(new Barnacle(x + worldX, y + worldY, down.getWorldHeight(), world));
                }
            }
        }
    }

    private void generateActivators(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (!isNullOrEmpty(layer.getCell(x, y))) {
                    // already has cell
                    continue;
                }

                WorldCell down = getCell(layer, x, y - 1, chunkI, chunkJ);
                if (down == WorldCell.EMPTY) {
                    if (Math.random() < 0.025) {
                        if (Math.random() < 0.7) {
                            TiledMapTile tile = getTile("object/storm-cloud2");
                            Body body = createBody(tile, world, x + worldX, y + worldY);
                            PhaseActivator a = new SolidActivator(tile, x + worldX, y + worldY, body);
                            WorldCell cell = new WorldCell(tile, x, y, a.getX(), a.getY(),
                                    Type.Activator);
                            layer.setCell(x, y, cell);
                            layer.addBody(body);
                        } else if (Math.random() < 0.95) {
                            TiledMapTile tile = getTile("object/cloud2");
                            Body body = createBody(tile, world, x + worldX, y + worldY);
                            PhaseActivator a = new LiquidActivator(tile, x + worldX, y + worldY, body);
                            WorldCell cell = new WorldCell(tile, x, y, a.getX(), a.getY(),
                                    Type.Activator);
                            layer.setCell(x, y, cell);
                            layer.addBody(body);
                        } else {
                            TiledMapTile tile = getTile("object/lightning-cloud2");
                            Body body = createBody(tile, world, x + worldX, y + worldY);
                            PhaseActivator a = new PlasmaActivator(tile, x + worldX, y + worldY, body);
                            WorldCell cell = new WorldCell(tile, x, y, a.getX(), a.getY(),
                                    Type.Activator);
                            layer.setCell(x, y, cell);
                            layer.addBody(body);
                        }
                    }
                }
            }
        }
    }

    private static Body createBody(TiledMapTile tile, World world, int x, int y) {
        return createBody(tile, world, x, y, 1);
    }

    private static Body createBody(TiledMapTile tile, World world, int x, int y, float heightScale) {
        float halfWidth = (tile.getTextureRegion().getRegionWidth() / 2.0f) * SCALE;
        float halfHeight = (heightScale * tile.getTextureRegion().getRegionHeight() / 2.0f) * SCALE;

        // create our body definition
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(x + halfWidth, y + halfHeight));
        Body groundBody = world.createBody(groundBodyDef);

        // create a polygon shape
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(halfWidth, halfHeight);

        // create the fixture
        FixtureDef def = new FixtureDef();
        def.shape = groundBox;
        def.isSensor = true;
        def.filter.categoryBits = 0x0001;
        def.filter.maskBits = ALL_BITS;
        groundBody.createFixture(def);

        // clean up
        groundBox.dispose();

        return groundBody;
    }

    private void generateWater(ChunkLayer layer, ChunkLayer terrain, int chunkI, int chunkJ,
            int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (!isNullOrEmpty(layer.getCell(x, y))) {
                    // already has cell
                    continue;
                }

                WorldCell current = getCell(terrain, x, y, chunkI, chunkJ);
                WorldCell down = getCell(terrain, x - 1, y - 1, chunkI, chunkJ);
                if (isTerrain(current) && isTerrain(down) && down.getSlope() == 0) {
                    // start of a valley, fill in water working back
                    Array<TilePoint> points = new Array<TilePoint>();
                    int localX = current.getLocalX();
                    int localY = current.getLocalY();
                    boolean finished = false;
                    while (!isNullOrEmpty(down) && !finished) {
                        points.add(TilePoint.of(localX, localY));

                        // set to new current
                        current = getCell(terrain, localX, localY, chunkI, chunkJ);
                        if (!isNullOrEmpty(current) && current.getSlope() < 0) {
                            // found the other end
                            finished = true;
                        }

                        // update down cell
                        localX -= 1;
                        down = getCell(terrain, localX, localY - 1, chunkI, chunkJ);
                    }

                    // liquid or lava
                    boolean isLiquid = Math.random() < 0.6;

                    // only add cells if we finished, otherwise we have an incomplete valley
                    if (finished) {
                        for (TilePoint point : points) {
                            float scaleY = 0.5f;
                            PhaseActivator activator;
                            if (isLiquid) {
                                TiledMapTile tile = getTile("water/top");
                                Body body = createBody(tile, world, point.x + worldX, point.y
                                        + worldY, scaleY);
                                activator = new LiquidActivator(tile, point.x + worldX, point.y
                                        + worldY, body);
                            } else {
                                TiledMapTile tile = getTile("lava/top");
                                Body body = createBody(tile, world, point.x + worldX, point.y
                                        + worldY, scaleY);
                                activator = new GasActivator(tile, point.x + worldX, point.y
                                        + worldY, body);
                            }

                            int tileX = activator.getX() - worldX;
                            int tileY = activator.getY() - worldY;
                            WorldCell cell = new WorldCell(activator.getTile(), tileX, tileY,
                                    activator.getX(), activator.getY(), Type.Activator, scaleY);
                            setCell(cell, cell.getLocalX(), cell.getLocalY(), chunkI, chunkJ, layer);
                            layer.addBody(activator.getBody());
                        }
                    }
                }
            }
        }
    }

    private void generateObstacles(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (!isNullOrEmpty(layer.getCell(x, y))) {
                    // already has cell
                    continue;
                }

                int localX = x;
                int localY = y;
                WorldCell down = getCell(layer, localX, localY - 1, chunkI, chunkJ);
                if (down == WorldCell.EMPTY && Math.random() < 0.025) {
                    // add a bridge
                    TiledMapTile tile;
                    short maskBits;
                    float scaleY;
                    boolean multiPart;
                    if (Math.random() < 0.5) {
                        tile = getTile("grass/bridge-logs");
                        maskBits = BIT_SOLID;
                        scaleY = 0.35f;
                        multiPart = false;
                    } else {
                        tile = getTile("grass/half-mid");
                        maskBits = ALL_BITS;
                        scaleY = 1.0f;
                        multiPart = true;
                    }
                    
                    WorldCell left = getCell(layer, localX - 1, localY, chunkI, chunkJ);
                    WorldCell right = getCell(layer, localX + 1, localY, chunkI, chunkJ);
                    while (down == WorldCell.EMPTY && left == WorldCell.EMPTY 
                            && right == WorldCell.EMPTY && Math.random() < 0.75) {
                        WorldCell cell = new WorldCell(tile, localX, localY, worldX + localX, worldY + localY,
                                Type.Platform, scaleY);
                        layer.setCell(localX, localY, cell);
                        
                        localX++;
                        down = getCell(layer, localX, localY - 1, chunkI, chunkJ);
                        right = getCell(layer, localX + 1, localY, chunkI, chunkJ);
                    }
                    
                    // add a body for the platform if empty space was found
                    int dx = localX - x;
                    if (dx > 0) {
                        Platform platform = new Platform(tile, worldX + x, worldY + y,
                                maskBits, world, dx, scaleY);
                        layer.addBody(platform.getBody());
                        
                        // update tiles
                        if (multiPart) {
                            // solid bridge
                            if (dx > 1) {
                                getCell(layer, x, y, chunkI, chunkJ).setTile(getTile("grass/half-left"));
                                getCell(layer, localX - 1, y, chunkI, chunkJ).setTile(getTile("grass/half-right"));
                            } else {
                                getCell(layer, x, y, chunkI, chunkJ).setTile(getTile("grass/half"));
                            }
                        }
                    }
                } else if (isTerrain(down) && down.getSlope() == 0 && rand.flip(0.075)) {
                    // add a wall
                    TiledMapTile tile = getTile("grass/hill-large");
                    WorldCell cell = new WorldCell(tile, localX, localY, worldX + localX,
                            worldY + localY, Type.Platform);
                    layer.setCell(localX, localY, cell);
                    Platform platform = new Platform(tile, worldX + x, worldY + y,
                            ALL_BITS, world, 1, 1, "water");
                    layer.addBody(platform.getBody());
                }
            }
        }
        
        // second pass
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                if (!isNullOrEmpty(layer.getCell(x, y))) {
                    // already has cell
                    continue;
                }
                
                int localX = x;
                int localY = y;
                WorldCell up = getCell(layer, localX, localY + 1, chunkI, chunkJ);
                if (!isNullOrEmpty(up) && up.getTile() == getTile("grass/bridge-logs") && Math.random() < 0.25) {
                    // log bridge -> build ropes
                    TiledMapTile tile = getTile("grass/rope-attached");
                    while (getCell(layer, localX, localY, chunkI, chunkJ) == WorldCell.EMPTY && Math.random() < 0.9) {
                        // sanity check: make sure the tile below is not an uphill slope, or else the player can get stuck
                        WorldCell down = getCell(layer, localX, localY - 1, chunkI, chunkJ);
                        if (!isNullOrEmpty(down) && down.getSlope() > 0) {
                            break;
                        }
                        
                        // place the next rope
                        WorldCell cell = new WorldCell(tile, localX, localY, worldX + localX,
                                worldY + localY, Type.Platform);
                        layer.setCell(localX, localY, cell);
                        
                        // only use the attached rope for the first piece
                        tile = getTile("grass/rope-vertical");
                        localY--;
                    }
                    
                    int dy = y - localY;
                    if (dy > 0) {
                        Platform platform = new Platform(tile, worldX + localX + 0.5f, worldY + localY + 1,
                                BIT_LIQUID, world, 0, dy, "water");
                        layer.addBody(platform.getBody());
                    }
                }
            }
        }
    }

    private void generateBackground(ChunkLayer layer, int chunkI, int chunkJ, int worldX, int worldY) {
        for (int x = 0; x < layer.getWidth(); x++) {
            // top -> bottom
            for (int y = layer.getHeight() - 1; y >= 0; y--) {
                if (!isNullOrEmpty(layer.getCell(x, y))) {
                    // already has cell
                    continue;
                }

                WorldCell up = getCell(layer, x, y + 1, chunkI, chunkJ);
                if (!isNullOrEmpty(up)) {
                    if (up.getType() == Type.Terrain || up.getType() == Type.Filler) {
                        StaticTiledMapTile tile = getTile("grass/center");
                        if (up.getSlope() < 0) {
                            tile = getTile("grass/hill-right2");
                        } else if (up.getSlope() > 0) {
                            tile = getTile("grass/hill-left2");
                        }
                        WorldCell cell = new WorldCell(tile, x, y, worldX + x, worldY + y,
                                Type.Filler);
                        layer.setCell(x, y, cell);
                    }
                }
            }
        }
    }

    private boolean isTerrain(WorldCell cell) {
        return cell != null && cell != WorldCell.EMPTY && cell.getType() == Type.Terrain;
    }
    
    private boolean isPlatform(WorldCell cell) {
        return cell != null && cell != WorldCell.EMPTY && cell.getType() == Type.Platform;
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
                lastTerrain = new WorldCell(getTile("grass/mid"), 0, 0, 0, 0, Type.Terrain);
                layer.setCell(0, 0, lastTerrain);
                terrainCells.add(lastTerrain);
                vertexCount++;
                layer.updateTerrainLimit(0);
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
                            worldY + y2, Type.Terrain, -1));
                }
                if (worldX > Settings.CHUNK_WIDTH * 2 && lastTerrain.matchesSlope(1, worldY + y2)) {
                    candidates.add(new WorldCell(getTile("grass/hill-left1"), x2, y2, worldX + x2,
                            worldY + y2, Type.Terrain, 1));
                }
                if (lastTerrain.matchesSlope(0, worldY + y2)) {
                    candidates.add(new WorldCell(getTile("grass/mid"), x2, y2, worldX + x2, worldY
                            + y2, Type.Terrain));
                }
            }

            if (candidates.size > 0) {
                WorldCell cell = candidates.get((int) (Math.random() * candidates.size));
                layer.setCell(cell.getLocalX(), cell.getLocalY(), cell);
                terrainCells.add(cell);
                vertexCount++;
                layer.updateTerrainLimit(cell.getLocalY());

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
                contactListener.endContact(chainBody);
                world.destroyBody(chainBody);
            }
            chainBody = world.createBody(bdef);
            Fixture fixture = chainBody.createFixture(fd);
            fixture.setUserData("ground");
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
            layer.updateTerrainLimit(cell.getLocalY());
        }
    }
    
    private boolean isNullOrEmpty(WorldCell cell) {
        return cell == null || cell == WorldCell.EMPTY;
    }

    private WorldCell getCell(ChunkLayer layer, int x, int y, int chunkI, int chunkJ) {
        // get the updated chunk
        int chunkX = (int) Math.floor(1.0 * x / width) + chunkJ;
        int chunkY = (int) Math.floor(1.0 * y / height) + chunkI;

        // handle out of bounds
        if (chunkX < 0 || chunkY < 0 || chunkX >= CHUNKS || chunkY >= CHUNKS) {
            return null;
        }

        // update layer if chunks differ
        if (chunkX != chunkJ || chunkY != chunkI) {
            if (chunks[chunkY][chunkX] == null
                    || chunks[chunkY][chunkX].getLayers().getCount() <= layer.getZ()) {
                // out of bounds
                return null;
            }
            layer = (ChunkLayer) chunks[chunkY][chunkX].getLayers().get(layer.getZ());
        }

        // return the cell within chunk
        int tileX = x - (chunkX - chunkJ) * width;
        int tileY = y - (chunkY - chunkI) * height;
        WorldCell cell = layer.getCell(tileX, tileY);
        return cell != null ? cell : WorldCell.EMPTY;
    }

    private void setCell(WorldCell cell, int x, int y, int chunkI, int chunkJ, ChunkLayer layer) {
        // get the updated chunk
        int chunkX = (int) Math.floor(1.0 * x / width) + chunkJ;
        int chunkY = (int) Math.floor(1.0 * y / height) + chunkI;

        // handle out of bounds
        if (chunkX < 0 || chunkY < 0 || chunkX >= CHUNKS || chunkY >= CHUNKS) {
            return;
        }

        // update layer if chunks differ
        if (chunkX != chunkJ || chunkY != chunkI) {
            if (chunks[chunkY][chunkX] == null
                    || chunks[chunkY][chunkX].getLayers().getCount() <= layer.getZ()) {
                // out of bounds
                return;
            }
            layer = (ChunkLayer) chunks[chunkY][chunkX].getLayers().get(layer.getZ());
        }

        // return the cell within chunk
        int tileX = x - (chunkX - chunkJ) * width;
        int tileY = y - (chunkY - chunkI) * height;
        layer.setCell(tileX, tileY, cell);
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
