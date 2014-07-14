package com.eldritch.hydrok.activator;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.eldritch.hydrok.player.Player.Phase;

public abstract class ObstaclePhaseActivator extends PhaseActivator {
    public ObstaclePhaseActivator(Phase phase, TiledMapTile tile, int x, int y, World world) {
        super(phase, tile, createBody(tile, world, x, y), x, y);
    }
    
    private static Body createBody(TiledMapTile tile, World world, int x, int y) {
        BodyDef cdef = new BodyDef();
        cdef.type = BodyType.StaticBody;
        float d = tile.getTextureRegion().getRegionWidth() * SCALE / 2;
        cdef.position.set(x + d, y + d);

        Body body = world.createBody(cdef);
        FixtureDef cfdef = new FixtureDef();
        CircleShape cshape = new CircleShape();
        cshape.setRadius(d / 2);
        cfdef.shape = cshape;
        cfdef.isSensor = true;
        cfdef.filter.categoryBits = 0x0001;
        cfdef.filter.maskBits = ALL_BITS;
        body.createFixture(cfdef);
        cshape.dispose();
        
        return body;
    }
    
    private static StaticTiledMapTile getTile(String asset) {
        return new StaticTiledMapTile(new TextureRegion(new Texture("collectables/" + asset
                + ".png")));
    }

    public static class WaterDroplet extends ObstaclePhaseActivator {
        public WaterDroplet(int x, int y, World world) {
            super(Phase.Liquid, getTile("water-droplet"), x, y, world);
        }
    }

    public static class IceShard extends ObstaclePhaseActivator {
        public IceShard(int x, int y, World world) {
            super(Phase.Solid, getTile("ice-shard"), x, y, world);
        }
    }

    public static class Fireball extends ObstaclePhaseActivator {
        public Fireball(int x, int y, World world) {
            super(Phase.Gas, getTile("fireball"), x, y, world);
        }
    }
}
