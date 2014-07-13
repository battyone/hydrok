package com.eldritch.hydrok.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.util.Settings;

public class LiquidManager extends AbstractPhaseManager {
    private static final int MAX_VELOCITY = 5;

    private final TextureRegion texture;

    public LiquidManager(Player player, World world, int x, int y, float width, float height) {
        super(player, world, x, y, width, height, 0.35f, 0.0f, Settings.BIT_LIQUID);
        texture = new TextureRegion(new Texture("sprite/liquid.png"));
    }

    @Override
    public void update(float delta, boolean grounded) {
        // apply right impulse, but only if on the ground max velocity is not reached yet
        Vector2 pos = getBody().getPosition();
        if (grounded && getBody().getLinearVelocity().x < MAX_VELOCITY) {
            getBody().applyLinearImpulse(0.075f, 0, pos.x, pos.y, true);
        }
    }

    @Override
    public void render(OrthogonalTiledMapRenderer renderer) {
        Vector2 position = getBody().getPosition();
        
        float width = getWidth();
        float height = getHeight();

        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(texture, position.x - width / 2, position.y - height / 2, width, height);
        batch.end();
    }
}
