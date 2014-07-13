package com.eldritch.hydrok.activator;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player;

public class Terminator implements Activator {
    private final Body body;
    private boolean gameOver = false;
    
    public Terminator(World world) {
        // create our body definition
        BodyDef groundBodyDef = new BodyDef();
        body = world.createBody(groundBodyDef);

        // create a polygon shape
        EdgeShape edge = new EdgeShape();
        
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

    @Override
    public void activate(Player player) {
        gameOver = true;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
