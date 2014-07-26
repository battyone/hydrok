package com.eldritch.hydrok.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.util.Settings;

public class SolidManager extends AbstractPhaseManager {
	private static final int MAX_VELOCITY = 8;
	private static final int MAX_VELOCITY_JUMP = 10;
	private static final float JUMP = 2.75f;
	
	private final TextureRegion texture;
	
	public SolidManager(Player player, World world, int x, int y, float width, float height) {
	    super(player, world, x, y, width, height, 0.5f, 0.3f, Settings.BIT_SOLID);
		texture = new TextureRegion(new Texture("sprite/solid.png"));
	}
	
	@Override
	public void applyImpulseFrom(float x, float y) {
	    // jump
	    Vector2 pos = getBody().getPosition();
	    Vector2 dir = new Vector2(pos.x, pos.y).sub(x, y).nor();
	    
	    float dx = dir.x * JUMP;
        float dy = dir.y * JUMP;
        if (!player.canJump) {
            // scale down jump when not on the ground
            dx *= 0.25f;
            dy *= 0.25f;
        }
	    if (Math.abs(getBody().getLinearVelocity().y) > MAX_VELOCITY_JUMP) {
            dy = 0;
        }
        if (Math.abs(getBody().getLinearVelocity().x) > MAX_VELOCITY_JUMP) {
            dx = 0;
        }
	    getBody().applyLinearImpulse(dx, dy, pos.x, pos.y, true);
	    player.canJump = false;
	}
	
	@Override
	public void doUpdate(float delta) {
		// apply right impulse, but only if on the ground and max velocity is not reached yet
		Vector2 pos = getBody().getPosition();
		if (player.isGrounded() && getBody().getLinearVelocity().x < MAX_VELOCITY) {
			getBody().applyLinearImpulse(0.15f, 0, pos.x, pos.y, true);
		}
	}
	
	@Override
	public void render(OrthogonalTiledMapRenderer renderer) {
	    Body body = getBody();
		Vector2 position = body.getPosition();
		
		float width = getWidth();
        float height = getHeight();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.setColor(1, 1, 1, getAlpha());
		batch.draw(texture, position.x - width / 2, position.y - height / 2, width / 2, height / 2,
				width, height, 1f, 1f, (float) (body.getAngle() * 180 / Math.PI));
		batch.setColor(Color.WHITE);
		batch.end();
	}
	
	private float getAlpha() {
	    return Math.min(1 - player.getTemperaturePercent() + 0.25f, 1);
	}
}
