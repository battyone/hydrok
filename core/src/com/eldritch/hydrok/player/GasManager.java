package com.eldritch.hydrok.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.eldritch.hydrok.GameScreen;
import com.eldritch.hydrok.util.Settings;

public class GasManager extends AbstractPhaseManager {
	private static final int MAX_VELOCITY = 3;
	
	private final Animation animation;
	private float stateTime = 0;
	
	public GasManager(World world, int x, int y) {
	    super(world, x, y, 0.25f, 0.3f, Settings.BIT_GAS);
	    
		TextureRegion[][] regions = GameScreen.getRegions("sprite/gas.png", 64, 64);
		Array<TextureRegion> allRegions = new Array<TextureRegion>();
		for (TextureRegion[] region : regions) {
			allRegions.addAll(region);
		}
        animation = new Animation(0.15f, allRegions);
        animation.setPlayMode(Animation.PlayMode.LOOP);
	}
	
	@Override
	public void update(float delta, boolean grounded) {
		stateTime += delta;
		
		// apply upwards impulse, but only if max velocity is not reached yet
		Vector2 pos = getBody().getPosition();
		if (getBody().getLinearVelocity().y < MAX_VELOCITY) {
			getBody().applyLinearImpulse(0, 0.10f, pos.x, pos.y, true);
		}
	}
	
	@Override
	public void render(OrthogonalTiledMapRenderer renderer) {
		Vector2 position = getBody().getPosition();
		
		float width = getWidth() * 2;
        float height = getHeight() * 2;
        
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(animation.getKeyFrame(stateTime),
				position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
	
	@Override
	public void setActive(boolean active) {
	    super.setActive(active);
		stateTime = 0;
	}
}
