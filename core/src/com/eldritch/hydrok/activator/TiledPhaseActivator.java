package com.eldritch.hydrok.activator;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player.Phase;

public abstract class TiledPhaseActivator extends PhaseActivator {
    public TiledPhaseActivator(Phase phase, TiledMapTile tile, int x, int y, World world) {
        super(phase, tile, createBody(tile, world, x, y), x, y);
    }
    
    private static Body createBody(TiledMapTile tile, World world, int x, int y) {
        float halfWidth = (tile.getTextureRegion().getRegionWidth() / 2.0f) * SCALE;
        float halfHeight = (tile.getTextureRegion().getRegionHeight() / 2.0f) * SCALE;
        
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
    
    public static class LiquidActivator extends TiledPhaseActivator {
        public LiquidActivator(TiledMapTile tile, int x, int y, World world) {
            super(Phase.Liquid, tile, x, y, world);
        }
    }

    public static class SolidActivator extends TiledPhaseActivator {
        public SolidActivator(TiledMapTile tile, int x, int y, World world) {
            super(Phase.Solid, tile, x, y, world);
        }
    }

    public static class GasActivator extends TiledPhaseActivator {
        public GasActivator(TiledMapTile tile, int x, int y, World world) {
            super(Phase.Gas, tile, x, y, world);
        }
    }
}
