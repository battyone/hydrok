package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.BIT_SOLID;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

public class WorldCell extends Cell {
    private final Type type;
    private final int slope;
    private final int localX;
    private final int localY;
    private final int worldX;
    private final int worldY;
    private final float worldHeight;
    private final Vector2 terrainVector;
    private WorldCell next = null;
    
    public WorldCell(TiledMapTile tile, int localX, int localY, int worldX, int worldY, Type type) {
        this(tile, localX, localY, worldX, worldY, type, 0);
    }

    public WorldCell(TiledMapTile tile, int localX, int localY, int worldX, int worldY, Type type, float scaleY) {
        this(tile, localX, localY, worldX, worldY, type, 0, scaleY);
    }

    public WorldCell(TiledMapTile tile, int localX, int localY, int worldX, int worldY, Type type, int slope) {
        this(tile, localX, localY, worldX, worldY, type, slope, 1);
    }
    
    public WorldCell(TiledMapTile tile, int localX, int localY, int worldX, int worldY, Type type, int slope, float scaleY) {
        this.type = type;
        this.slope = slope;
        this.localX = localX;
        this.localY = localY;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldHeight = tile != null ? tile.getTextureRegion().getRegionHeight() * SCALE * scaleY : 0;
        terrainVector = type == Type.Terrain ? new Vector2(worldX, worldY + vy()) : Vector2.Zero;
        setTile(tile);
    }
    
    public float getWorldHeight() {
        return worldHeight;
    }
    
    public int getLocalX() {
        return localX;
    }
    
    public int getLocalY() {
        return localY;
    }
    
    public int getWorldX() {
        return worldX;
    }
    
    public int getWorldY() {
        return worldY;
    }
    
    public Vector2 getTerrainVector() {
        return terrainVector;
    }

    public Type getType() {
        return type;
    }

    public int getSlope() {
        return slope;
    }
    
    public boolean hasNext() {
        return next != null;
    }
    
    public void setNext(WorldCell next) {
        this.next = next;
    }
    
    public WorldCell next() {
        return next;
    }
    
    public boolean matchesSlope(int otherSlope, int otherY) {
        int dy = otherY - worldY;
        if (otherSlope > 0) {
            // going uphill
            return slope >= 0 && otherSlope == dy;
        } else if (slope > 0) {
            // was going uphill
            return (otherSlope > 0 && otherSlope == dy) || dy == 0;
        }
        return slope == dy;
    }
    
    public int vy() {
        return slope > 0 ? 0 : 1;
    }

    public enum Type {
        Terrain(ALL_BITS), Platform(BIT_SOLID), Filler(ALL_BITS), Activator(ALL_BITS);
        
        private final short maskBits;
        
        private Type(short maskBits) {
            this.maskBits = maskBits;
        }
        
        public short getMaskBits() {
            return maskBits;
        }
    }
    
    public static final WorldCell EMPTY = new WorldCell(null, -1, -1, -1, -1, Type.Filler);
}
