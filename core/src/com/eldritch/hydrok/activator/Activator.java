package com.eldritch.hydrok.activator;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player;
import com.eldritch.hydrok.player.Player.Phase;

public interface Activator {
    void activate(Player player);
    
    Body getBody();
    
    TiledMapTile getTile();
    
    int getX();
    
    int getY();
    
    public static abstract class AbstractActivator implements Activator {
        private final TiledMapTile tile;
        private final Body body;
        private final int x;
        private final int y;
        
        public AbstractActivator(String asset, int x, int y, World world) {
            this.x = x;
            this.y = y;
            
            tile = new StaticTiledMapTile(
                    new TextureRegion(new Texture("collectables/" + asset + ".png")));
            
            BodyDef cdef = new BodyDef();
            cdef.type = BodyType.StaticBody;
            float d = tile.getTextureRegion().getRegionWidth() * SCALE / 2;
            cdef.position.set(x + d, y + d);
            
            body = world.createBody(cdef);
            FixtureDef cfdef = new FixtureDef();
            CircleShape cshape = new CircleShape();
            cshape.setRadius(d / 2);
            cfdef.shape = cshape;
            cfdef.isSensor = true;
            cfdef.filter.categoryBits = 0x0001;
            cfdef.filter.maskBits = ALL_BITS;
            body.createFixture(cfdef).setUserData(this);
            cshape.dispose();
        }
        
        @Override
        public Body getBody() {
            return body;
        }
        
        @Override
        public TiledMapTile getTile() {
            return tile;
        }
        
        @Override
        public int getX() {
            return x;
        }
        
        @Override
        public int getY() {
            return y;
        }
    }
    
    public static class WaterDroplet extends AbstractActivator {
        public WaterDroplet(int x, int y, World world) {
            super("water-droplet", x, y, world);
        }
        
        @Override
        public void activate(Player player) {
            player.setPhase(Phase.Liquid);
        }
    }
    
    public static class IceShard extends AbstractActivator {
        public IceShard(int x, int y, World world) {
            super("ice-shard", x, y, world);
        }
        
        @Override
        public void activate(Player player) {
            player.setPhase(Phase.Solid);
        }
    }
    
    public static class Fireball extends AbstractActivator {
        public Fireball(int x, int y, World world) {
            super("fireball", x, y, world);
        }
        
        @Override
        public void activate(Player player) {
            player.setPhase(Phase.Gas);
        }
    }
}
