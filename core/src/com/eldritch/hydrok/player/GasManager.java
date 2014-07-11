package com.eldritch.hydrok.player;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.eldritch.hydrok.GameScreen;
import com.eldritch.hydrok.player.Player.PhaseManager;

public class GasManager implements PhaseManager {
	private static final int MAX_VELOCITY = 7;
	
	private final Body body;
	private final Animation animation;
	private final float width;
	private final float height;
	private float stateTime = 0;
	
	public GasManager(World world, int x, int y) {
		TextureRegion[][] regions = GameScreen.getRegions("sprite/gas.png", 64, 64);
		Array<TextureRegion> allRegions = new Array<TextureRegion>();
		for (TextureRegion[] region : regions) {
			allRegions.addAll(region);
		}
        animation = new Animation(0.15f, allRegions);
        animation.setPlayMode(Animation.PlayMode.LOOP);
		
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
		fixtureDef.density = 0.25f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.3f; // Make it bounce a little bit

		// Create our fixture and attach it to the body
		body.createFixture(fixtureDef);

		// rendering dimensions
//		width = SCALE * texture.getWidth();
//		height = SCALE * texture.getHeight();
		width = circle.getRadius() * 4;
		height = circle.getRadius() * 4;
		
		// Remember to dispose of any shapes after you're done with them!
		// BodyDef and FixtureDef don't need disposing, but shapes do.
		circle.dispose();
		
		setActive(false);
	}
	
	@Override
	public void update(float delta) {
		stateTime += delta;
		
		// apply upwards impulse, but only if max velocity is not reached yet
		Vector2 pos = getBody().getPosition();
		if (getBody().getLinearVelocity().y < MAX_VELOCITY) {
			body.applyLinearImpulse(0, 0.10f, pos.x, pos.y, true);
		}
	}
	
	@Override
	public void render(OrthogonalTiledMapRenderer renderer) {
		Vector2 position = body.getPosition();
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(animation.getKeyFrame(stateTime),
				position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}

	@Override
	public Body getBody() {
		return body;
	}
	
	@Override
	public void setActive(boolean active) {
		stateTime = 0;
		body.setActive(active);
	}
}
