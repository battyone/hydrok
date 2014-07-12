package com.eldritch.hydrok.player;

import static com.eldritch.hydrok.util.Settings.SCALE;

import java.util.EnumMap;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Player {
    private final Body body;
    private final float width;
    private final float height;
	private final EnumMap<Phase, PhaseManager> managers;
	
	// mutable state
	private Phase phase = Phase.Solid;

	public Player(World world, int x, int y) {
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
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.7f;
        fixtureDef.restitution = 0.5f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("player");

        // set collision masks
        Filter filter = fixture.getFilterData();
        filter.categoryBits = 0x0000;
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
        
        // create phase managers
		managers = new EnumMap<Phase, PhaseManager>(Phase.class);
		managers.put(Phase.Solid, new SolidManager(this, world, x, y, width, height));
		managers.put(Phase.Liquid, new LiquidManager(this, world, x, y, width, height));
		managers.put(Phase.Gas, new GasManager(this, world, x ,y, width * 2, height * 2));
		managers.get(phase).setActive();
	}

	public void update(float delta, boolean grounded) {
	    // update phase manager
		managers.get(phase).update(delta, grounded);
	}

	public void render(OrthogonalTiledMapRenderer renderer) {
		managers.get(phase).render(renderer);
	}
	
	public Vector2 getPosition() {
		return getBody().getPosition();
	}

	public Body getBody() {
		return body;
	}
	
	public void setPhase(Phase phase) {
		if (this.phase == phase) {
			return;
		}
		
		this.phase = phase;
		managers.get(phase).setActive();
	}
	
	public enum Phase {
		Solid, Liquid, Gas, Plasma
	}

	public static interface PhaseManager {
		void update(float delta, boolean grounded);

		void render(OrthogonalTiledMapRenderer renderer);
		
		Player getPlayer();

		Body getBody();
		
		void setActive();
	}
}
