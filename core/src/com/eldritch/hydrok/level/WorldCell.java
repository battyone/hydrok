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
    private final int x;
    private final int y;
    private WorldCell next = null;

    public WorldCell(TiledMapTile tile, int x, int y, World world, Type type) {
        this(tile, x, y, world, type, 0);
    }

    public WorldCell(TiledMapTile tile, int x, int y, World world, Type type, int slope) {
        this.type = type;
        this.slope = slope;
        body = createBody(world, x, y, tile, type, slope);
        this.x = x;
        this.y = y;
        setTile(tile);
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
    
    public boolean matchesSlope(int dy) {
        // in order to match the slope, our slope must be the inverse of the delta
        return slope == -dy;
    }
    
    private static Body createBody(World world, int x, int y, TiledMapTile tile, Type type, int slope) {
        switch (type) {
            case Terrain:
            case Filler:
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
        Terrain(ALL_BITS), Platform(BIT_SOLID), Filler(ALL_BITS);
        
        private final short maskBits;
        
        private Type(short maskBits) {
            this.maskBits = maskBits;
        }
        
        public short getMaskBits() {
            return maskBits;
        }
    }
}
