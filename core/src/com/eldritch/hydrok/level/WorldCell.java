package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.BIT_SOLID;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class WorldCell extends Cell {
    private final Type type;
    private final int slope;
    private final Body body;
    private final int localX;
    private final int localY;
    private final int worldX;
    private final int worldY;
    private WorldCell next = null;

    public WorldCell(TiledMapTile tile, int localX, int localY, int worldX, int worldY, World world, Type type) {
        this(tile, localX, localY, worldX, worldY, world, type, 0);
    }

    public WorldCell(TiledMapTile tile, int localX, int localY, int worldX, int worldY, World world, Type type, int slope) {
        this.type = type;
        this.slope = slope;
        body = createBody(world, worldX, worldY, tile, type, slope);
        this.localX = localX;
        this.localY = localY;
        this.worldX = worldX;
        this.worldY = worldY;
        setTile(tile);
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

    public Type getType() {
        return type;
    }

    public int getSlope() {
        return slope;
    }
    
    public boolean hasBody() {
        return body != null;
    }
    
    public Body getBody() {
        return body;
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
//            System.out.println(String.format("slope: %d, dy: %d", slope, dy));
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
    
    private static Body createBody(World world, int x, int y, TiledMapTile tile, Type type, int slope) {
        switch (type) {
            case Terrain:
            case Filler:
            case Collectable:
                return null;
            case Platform:
                return createBox(world, x, y, tile, type);
            default:
                throw new IllegalStateException("Unrecognized type: " + type);
        }
    }
    
    private static Body createBox(World world, int x, int y, TiledMapTile tile, Type type) {
        float halfWidth = (tile.getTextureRegion().getRegionWidth() / 2.0f) * SCALE;
        float halfHeight = (tile.getTextureRegion().getRegionHeight() / 2.0f) * SCALE;
        return createBox(world, x, y, halfWidth, halfHeight, type.maskBits);
    }

    private static Body createBox(World world, int x, int y, float halfWidth, float halfHeight,
            short maskBits) {
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
        Fixture fixture = groundBody.createFixture(groundBox, 0.0f);

        // set collision masks
        Filter filter = fixture.getFilterData();
        filter.categoryBits = 0x0001;
        filter.maskBits = maskBits;
        fixture.setFilterData(filter);

        // Clean up after ourselves
        groundBox.dispose();

        return groundBody;
    }

    public enum Type {
        Terrain(ALL_BITS), Platform(BIT_SOLID), Filler(ALL_BITS), Collectable(ALL_BITS);
        
        private final short maskBits;
        
        private Type(short maskBits) {
            this.maskBits = maskBits;
        }
        
        public short getMaskBits() {
            return maskBits;
        }
    }
}
