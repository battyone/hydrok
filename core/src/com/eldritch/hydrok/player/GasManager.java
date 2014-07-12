package com.eldritch.hydrok.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.eldritch.hydrok.GameScreen;
import com.eldritch.hydrok.util.Settings;

public class GasManager extends AbstractPhaseManager {
	private static final int MAX_VELOCITY = 3;
	
	private final Animation animation;
	private float stateTime = 0;
	private float lastGround = 0;
	
	public GasManager(Body body, World world, int x, int y, float width, float height) {
	    super(body, world, x, y, width, height, 0.20f, 0.3f, Settings.BIT_GAS);
	    
		TextureRegion[][] regions = GameScreen.getRegions("sprite/gas.png", 64, 64);
		Array<TextureRegion> allRegions = new Array<TextureRegion>();
		for (TextureRegion[] region : regions) {
			allRegions.addAll(region);
		}
        animation = new Animation(0.15f, allRegions);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        
        // air resistance to slow down the body
        getBody().setLinearDamping(0.5f);
	}
	
	@Override
	public void update(float delta, boolean grounded) {
		stateTime += delta;
		if (grounded) {
		    lastGround = 0;
		}
		lastGround += delta;
		
		// apply upwards impulse, but only if max velocity is not reached yet
		Body body = getBody();
		Vector2 pos = body.getPosition();
		if (body.getLinearVelocity().y < MAX_VELOCITY) {
		    float force = Math.max(1 / (lastGround * 100), 0.01f);
			body.applyLinearImpulse(0, force, pos.x, pos.y, true);
		}
	}
	
	@Override
	public void render(OrthogonalTiledMapRenderer renderer) {
		Vector2 position = getBody().getPosition();
		
		float width = getWidth();
        float height = getHeight();
        
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(animation.getKeyFrame(stateTime),
				position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
	
	@Override
	public void setActive() {
	    super.setActive();
		stateTime = 0;
	}
}
