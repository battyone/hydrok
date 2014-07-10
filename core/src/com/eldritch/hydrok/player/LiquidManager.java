package com.eldritch.hydrok.player;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player.PhaseManager;

public class LiquidManager implements PhaseManager {
	private final Body body;
	private final TextureRegion texture;
	private final float width;
	private final float height;
	
	public LiquidManager(World world) {
		texture = new TextureRegion(new Texture("sprite/liquid.png"));
		
		// First we create a body definition
		BodyDef bodyDef = new BodyDef();
		
		// We set our body to dynamic, for something like ground which doesn't
		// move we would set it to StaticBody
		bodyDef.type = BodyType.DynamicBody;
		
		// Set our body's starting position in the world
		bodyDef.position.set(3, 10);

		// Create our body in the world using our body definition
		body = world.createBody(bodyDef);

		// Create a circle shape and set its radius to 6
		CircleShape circle = new CircleShape();
		circle.setRadius(12f * SCALE);

		// Create a fixture definition to apply our shape to
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.35f;
		fixtureDef.friction = 0.7f;
		fixtureDef.restitution = 0.3f; // Make it bounce a little bit

		// Create our fixture and attach it to the body
		body.createFixture(fixtureDef);

		// rendering dimensions
//		width = SCALE * texture.getWidth();
//		height = SCALE * texture.getHeight();
		width = circle.getRadius() * 2;
		height = circle.getRadius() * 2;
		
		// Remember to dispose of any shapes after you're done with them!
		// BodyDef and FixtureDef don't need disposing, but shapes do.
		circle.dispose();
		
		setActive(false);
	}
	
	@Override
	public void update(float delta) {
	}
	
	@Override
	public void render(OrthogonalTiledMapRenderer renderer) {
		Vector2 position = body.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(texture, position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
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
