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
import com.eldritch.hydrok.util.HydrokContactListener;

public class Player {
    private static final int TEMP_SCALE = 10;
    
    private final Body body;
    private final float width;
    private final float height;
	private final EnumMap<Phase, PhaseManager> managers;
	private final HydrokContactListener contactListener;
	float lastGround = 0;
	boolean grounded = true;
	boolean canJump = true;
	float temperature = 0;
	
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
        
        // add foot sensor fixture
//        bodyDef = new BodyDef();
//        bodyDef.type = BodyType.KinematicBody;
//        bodyDef.position.set(x, y);
//        footBody = world.createBody(bodyDef);
//        
//        PolygonShape polygon = new PolygonShape();;
//        polygon.setAsBox(0.1f, 0.25f, new Vector2(0, -0.5f), 0);
//        fixtureDef = new FixtureDef();
//        fixtureDef.shape = polygon;
//        fixtureDef.isSensor = true;
//        fixture = body.createFixture(fixtureDef);
//        fixture.setUserData("foot");
//        polygon.dispose();
        
        // create phase managers
		managers = new EnumMap<Phase, PhaseManager>(Phase.class);
		managers.put(Phase.Solid, new SolidManager(this, world, x, y, width, height));
		managers.put(Phase.Liquid, new LiquidManager(this, world, x, y, width, height));
		managers.put(Phase.Gas, new GasManager(this, world, x ,y, width * 2, height * 2));
		managers.put(Phase.Plasma, new PlasmaManager(this, world, x ,y, width * 2, height * 2));
		managers.get(phase).setActive();
		
		contactListener = new HydrokContactListener(this);
	}
	
	public HydrokContactListener getContactListener() {
	    return contactListener;
	}
	
	public boolean isGrounded() {
	    return contactListener.isGrounded();
	}
	
	public boolean isWaterGrounded() {
	    return contactListener.isWaterGrounded();
	}

	public void update(float delta) {
	    // update temperature
	    temperature = Math.max(temperature - TEMP_SCALE * delta, 0);
	    Phase next = phase.next();
	    Phase previous = phase.previous();
	    if (next != null && temperature >= next.getTemperature()) {
	        transition(next);
	    } else if (previous != null && temperature <= previous.getTemperature()) {
	        transition(previous);
	    }
	    
	    // update grounded state
	    this.grounded = contactListener.isGrounded();
	    if (grounded && phase != Phase.Gas) {
            lastGround = 0;
        }
	    lastGround += delta;
	    
	    // update phase manager
		managers.get(phase).update(delta);
	}

	public void render(OrthogonalTiledMapRenderer renderer) {
		managers.get(phase).render(renderer);
	}
	
	public float getRelativeTemperature() {
	    return temperature - phase.getTemperature();
	}
	
	public float getTemperaturePercent() {
	    Phase next = phase.next();
	    if (next == null) {
	        return 0;
	    }
	    return getRelativeTemperature() / (next.getTemperature() - phase.getTemperature());
	}
	
	public float getPreviousPercent() {
	    Phase previous = phase.previous();
        if (previous == null) {
            return 0;
        }
        return Math.abs(getRelativeTemperature() / (phase.getTemperature() - previous.getTemperature()));
	}
	
	public Vector2 getPosition() {
		return body.getPosition();
	}
	
	public Vector2 getVelocity() {
	    return body.getLinearVelocity();
	}

	public Body getBody() {
		return body;
	}
	
	public void markGrounded() {
	    canJump = true;
	}
	
	public void applyImpulseFrom(float x, float y) {
	    temperature += TEMP_SCALE;
	    managers.get(phase).applyImpulseFrom(x, y);
	}
	
	public void stop() {
	    body.setLinearVelocity(0, 0);
	}
	
	public void applyImpulse(float x, float y) {
	    Vector2 pos = body.getPosition();
	    body.applyLinearImpulse(x, y, pos.x, pos.y, true);
	}
	
	public void transition(Phase nextPhase) {
	    // transition
	    setPhase(nextPhase);
	}
	
	public void setPhase(Phase phase) {
	    temperature = phase.getTemperature();
		if (this.phase == phase) {
			return;
		}
		
		this.phase = phase;
		managers.get(phase).setActive();
	}
	
	public Phase getPhase() {
	    return phase;
	}
	
	public enum Phase {
		Solid(0), Liquid(30), Gas(60), Plasma(500);
		
		private final int temperature;
		
		private Phase(int temperature) {
		    this.temperature = temperature;
		}
		
		public int getTemperature() {
		    return temperature;
		}
		
		public Phase next() {
		    if (ordinal() == Phase.values().length - 1) {
		        return null;
		    }
		    return Phase.values()[ordinal() + 1];
		}
		
		public Phase previous() {
            if (ordinal() == 0) {
                return null;
            }
            return Phase.values()[ordinal() - 1];
        }
	}

	public static interface PhaseManager {
		void update(float delta);

		void render(OrthogonalTiledMapRenderer renderer);
		
		Player getPlayer();

		Body getBody();
		
		void setActive();
		
		void applyImpulseFrom(float x, float y);
	}
}
