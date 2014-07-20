package com.eldritch.hydrok.level;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class ChunkLayer extends TiledMapTileLayer {
    private final Set<Body> bodies = new HashSet<Body>();
    private final World world;
    private final int z;
    private int terrainLimit = -1;

    public ChunkLayer(World world, int width, int height, int tileWidth, int tileHeight, int z) {
        super(width, height, tileWidth, tileHeight);
        this.world = world;
        this.z = z;
    }
    
    public int getZ() {
        return z;
    }

    public void addBody(Body body) {
        bodies.add(body);
    }
    
    public void updateTerrainLimit(int y) {
        terrainLimit = Math.max(terrainLimit, y);
    }
    
    public int getTerrainLimit() {
        return terrainLimit;
    }
    
    @Override
    public void setCell(int x, int y, Cell cell) {
        if (cell instanceof WorldCell) {
            setCell(x, y, (WorldCell) cell);
        } else {
            throw new IllegalArgumentException("Can only add WorldCells to the ChunkLayer");
        }
    }

    public void setCell(int x, int y, WorldCell cell) {
        super.setCell(x, y, cell);
    }

    @Override
    public WorldCell getCell(int x, int y) {
        Cell cell = super.getCell(x, y);
        return cell != null ? (WorldCell) cell : null;
    }

    public void destroy() {
        for (Body body : bodies) {
            world.destroyBody(body);
        }
    }
}
