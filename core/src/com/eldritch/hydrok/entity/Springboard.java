package com.eldritch.hydrok.entity;

import static com.eldritch.hydrok.util.Settings.BIT_SOLID;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.eldritch.hydrok.player.Player;

public class Springboard implements Entity {
    private static final float V = 5;
    private final Body body;
    private final float width;
    private final float height;
    private final TextureRegion downTexture;
    private final TextureRegion upTexture;
    private boolean sprung = false;
    
    public Springboard(TextureRegion down, TextureRegion up, int x, int y, float offsetY, World world) {
        downTexture = down;
        upTexture = up;
        
        float h = downTexture.getRegionHeight() * SCALE * 0.5f;
        float worldY = y + h;
        worldY += offsetY / 2;
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(x, worldY);
        body = world.createBody(bodyDef);

        float r = (Math.min(downTexture.getRegionWidth(), downTexture.getRegionHeight()) / 2) * SCALE;
        CircleShape circle = new CircleShape();
        circle.setRadius(r);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        Filter filter = fixture.getFilterData();
        filter.categoryBits = 0x0001;
        filter.maskBits = BIT_SOLID;
        fixture.setFilterData(filter);

        width = downTexture.getRegionWidth() * SCALE;
        height = downTexture.getRegionHeight() * SCALE;

        circle.dispose();
    }
    
    @Override
    public void update(float delta) {
    }
    
    @Override
    public void render(OrthogonalTiledMapRenderer renderer) {
        TextureRegion texture = sprung ? upTexture : downTexture;
        
        Vector2 position = body.getPosition();
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(texture,
                position.x - width / 2, position.y - height / 2, // position
                width / 2, height / 2, // origin
                width, height, // size
                1, 1, // scale
                0);
        batch.end();
    }

    @Override
    public void dispose(World world) {
        world.destroyBody(body);
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void activate(Player player) {
        player.applyImpulse(0, getImpulse());
        sprung = true;
    }

    @Override
    public Body getBody() {
        return body;
    }
    
    private float getImpulse() {
        return sprung ? V * 0.5f : V;
    }
}
