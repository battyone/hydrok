package com.eldritch.hydrok.entity;

import static com.eldritch.hydrok.util.Settings.BIT_SOLID;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Texture;
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

public class Fly implements Entity {
    private static final float V = -1.5f;
    private final Body body;
    private final float width;
    private final float height;
    private final TextureRegion texture;
    
    public Fly(int x, int y, World world) {
        texture = new TextureRegion(new Texture("sprite/fly.png"));
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KinematicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        float d = Math.min(texture.getRegionWidth(), texture.getRegionHeight()) / 2;
        CircleShape circle = new CircleShape();
        circle.setRadius(d * SCALE);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.7f;
        fixtureDef.restitution = 0.5f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        Filter filter = fixture.getFilterData();
        filter.categoryBits = 0x0001;
        filter.maskBits = BIT_SOLID;
        fixture.setFilterData(filter);

        width = texture.getRegionWidth() * SCALE;
        height = texture.getRegionHeight() * SCALE;

        circle.dispose();
    }
    
    @Override
    public void update(float delta) {
        float nextX = body.getPosition().x + delta * V;
        body.setTransform(nextX, body.getPosition().y, 0);
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
    public void dispose(World world) {
        world.destroyBody(body);
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void activate(Player player) {
        player.applyImpulse(-5, 0);
    }

    @Override
    public Body getBody() {
        return body;
    }
}
