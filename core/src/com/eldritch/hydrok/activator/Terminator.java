package com.eldritch.hydrok.activator;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.level.ProceduralTiledMap;
import com.eldritch.hydrok.player.Player;

public class Terminator implements Activator {
    private static final float V = 2.5f;
    
    private final Body body;
    private final ProceduralTiledMap map;
    private boolean gameOver = false;
    private int lastX;
    
    public Terminator(World world, ProceduralTiledMap map) {
        this.map = map;
        lastX = map.getMinX();
        
        // create our body definition
        BodyDef groundBodyDef = new BodyDef();
        body = world.createBody(groundBodyDef);

        // create a polygon shape
        EdgeShape edge = new EdgeShape();
        Vector2 v1 = new Vector2(map.getMinX(), map.getMinY());
        Vector2 v2 = new Vector2(map.getMinX(), map.getMinY() + map.getHeight());
        edge.set(v1, v2);
        
        // create the fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edge;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = 0x0001;
        fixtureDef.filter.maskBits = ALL_BITS;
        body.createFixture(fixtureDef).setUserData(this);

        // clean up after ourselves
        edge.dispose();
    }
    
    public void update(float delta) {
        float nextX = body.getPosition().x + delta * V;
        if (map.getMinX() != lastX) {
            nextX = Math.max(map.getMinX(), nextX);
            lastX = map.getMinX();
        }
        body.setTransform(nextX, map.getMinY(), 0);
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void activate(Player player) {
        HydrokGame.log("GAME OVER!!");
        gameOver = true;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
