package com.eldritch.hydrok.agent;

import static com.eldritch.hydrok.util.Settings.SCALE;

import java.util.EnumMap;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
	public final Body body;
	private final EnumMap<Phase, PhaseManager> managers;
	private Phase phase = Phase.Solid;
	
	public Player(World world) {
		managers = new EnumMap<Phase, PhaseManager>(Phase.class);
		managers.put(Phase.Solid, new SolidManager());
		
		// First we create a body definition
		BodyDef bodyDef = new BodyDef();
		
		// We set our body to dynamic, for something like ground which doesn't
		// move we would set it to StaticBody
		bodyDef.type = BodyType.DynamicBody;
		
		// Set our body's starting position in the world
		bodyDef.position.set(20, 20);

		// Create our body in the world using our body definition
		body = world.createBody(bodyDef);

		// Create a circle shape and set its radius to 6
		CircleShape circle = new CircleShape();
		circle.setRadius(6f * SCALE);

		// Create a fixture definition to apply our shape to
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f; // Make it bounce a little bit

		// Create our fixture and attach it to the body
		Fixture fixture = body.createFixture(fixtureDef);

		// Remember to dispose of any shapes after you're done with them!
		// BodyDef and FixtureDef don't need disposing, but shapes do.
		circle.dispose();
	}
	
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		managers.get(phase).render(this, delta, renderer);
	}
	
	public enum Phase {
		Solid, Liquid, Gas, Plasma
	}
	
	public static interface PhaseManager {
		void render(Player player, float delta, OrthogonalTiledMapRenderer renderer);
	}
}
