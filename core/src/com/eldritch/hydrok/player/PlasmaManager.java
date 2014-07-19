package com.eldritch.hydrok.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.util.Settings;

public class PlasmaManager extends AbstractPhaseManager {
    private static final int MAX_VELOCITY = 12;
    private static final float JUMP = 3f;
    
    private final TextureRegion texture;
    
    public PlasmaManager(Player player, World world, int x, int y, float width, float height) {
        super(player, world, x, y, width, height, 0.3f, 0, Settings.BIT_PLASMA);
        texture = new TextureRegion(new Texture("sprite/plasma.png"));
    }
    
    @Override
    public void applyImpulseFrom(float x, float y) {
        // jump
        Vector2 pos = getBody().getPosition();
        Vector2 dir = new Vector2(pos.x, pos.y).sub(x, y).nor();
        getBody().applyLinearImpulse(dir.x * JUMP, dir.y * JUMP, pos.x, pos.y, true);
        getPlayer().canJump = false;
    }
    
    @Override
    public void doUpdate(float delta, boolean grounded) {
        // apply right impulse, but only if max velocity is not reached yet
        Vector2 pos = getBody().getPosition();
        if (getBody().getLinearVelocity().x < MAX_VELOCITY) {  
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
        batch.draw(texture, position.x - width / 2, position.y - height / 2, width, height);
        batch.end();
    }
}
