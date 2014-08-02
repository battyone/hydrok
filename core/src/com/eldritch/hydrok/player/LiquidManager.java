package com.eldritch.hydrok.player;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.util.Settings;

public class LiquidManager extends AbstractPhaseManager {
    private static final int MAX_VELOCITY_X = 2;
    private static final int MAX_VELOCITY_JUMP = 5;
    private static final float JUMP = 3.25f;

    private final TextureRegion texture;

    public LiquidManager(Player player, World world, int x, int y, float width, float height) {
        super(player, world, x, y, width, height, 0.35f, 0.0f, Settings.BIT_LIQUID);
        texture = new TextureRegion(new Texture("sprite/liquid.png"));
    }
    
    @Override
    public void applyImpulseFrom(float x, float y) {
        Vector2 pos = getBody().getPosition();
        Vector2 dir = new Vector2(pos.x, pos.y).sub(x, y).nor();
        
        float dx = dir.x * JUMP;
        float dy = dir.y * JUMP;
        if (!canJump(dy) || Math.abs(getBody().getLinearVelocity().y) > MAX_VELOCITY_JUMP) {
            // can't jump as liquid
            dy = 0.5f;
        }
        if (Math.abs(getBody().getLinearVelocity().x) > MAX_VELOCITY_JUMP) {
            dx = 0.5f;
        }
        getBody().applyLinearImpulse(dx, dy, pos.x, pos.y, true);
    }
    
    private boolean canJump(float dy) {
        return dy <= 0 || player.isWaterGrounded();
    }

    @Override
    public void doUpdate(float delta) {
        // apply right impulse, but only if on the ground max velocity is not reached yet
        Vector2 pos = getBody().getPosition();
        if (player.isGrounded() && getBody().getLinearVelocity().x < MAX_VELOCITY_X) {
            getBody().applyLinearImpulse(0.075f, 0, pos.x, pos.y, true);
        }
    }

    @Override
    public void render(OrthogonalTiledMapRenderer renderer) {
        Vector2 position = getBody().getPosition();
        
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;
        float intensity = getIntensity();
        float alpha = getAlpha();

        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.setColor(intensity, intensity, intensity, alpha);
        batch.draw(texture, position.x - width / 2, position.y - height / 2, width, height);
        batch.setColor(Color.WHITE);
        batch.end();
    }
    
    private float getIntensity() {
        return Math.min(1 - player.getTemperaturePercent() + 0.25f, 1);
    }
    
    private float getAlpha() {
        return Math.min(1 - player.getPreviousPercent() + 0.25f, 1);
    }
}
