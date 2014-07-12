package com.eldritch.hydrok.player;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player.PhaseManager;

public abstract class AbstractPhaseManager implements PhaseManager {
    private final Body body;
    private final float width;
    private final float height;

    // TODO: should change to only one body for all phases to fix the contact listener end contact
    // bug
    public AbstractPhaseManager(World world, int x, int y, float density, float restitution,
            short categoryBits) {

        // First we create a body definition
        BodyDef bodyDef = new BodyDef();

        // We set our body to dynamic, for something like ground which doesn't
        // move we would set it to StaticBody
        bodyDef.type = BodyType.DynamicBody;

        // Set our body's starting position in the world
        bodyDef.position.set(x, y);

        // Create our body in the world using our body definition
        body = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(22f * SCALE);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = 0.7f;
        fixtureDef.restitution = restitution; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("player");

        // set collision masks
        Filter filter = fixture.getFilterData();
        filter.categoryBits = categoryBits;
        filter.maskBits = 0x0001;
        fixture.setFilterData(filter);

        // rendering dimensions
        // width = SCALE * texture.getWidth();
        // height = SCALE * texture.getHeight();
        width = circle.getRadius() * 2;
        height = circle.getRadius() * 2;

        // Remember to dispose of any shapes after you're done with them!
        // BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();

        setActive(false);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setActive(boolean active) {
        body.setActive(active);
    }
}
