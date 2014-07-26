package com.eldritch.hydrok.entity;

import static com.eldritch.hydrok.util.Settings.BIT_LIQUID;
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

public class Barnacle implements Entity {
    private static final float V = 5;
    private final Body body;
    private final float width;
    private final float height;
    private final TextureRegion texture;
    private final boolean up;
    private final int sign;
    
    public Barnacle(int x, int y, float offsetY, boolean up, World world) {
        texture = new TextureRegion(new Texture("sprite/barnacle.png"));
        this.up = up;
        this.sign = up ? 1 : -1;
        
        float h = texture.getRegionHeight() * SCALE * 0.5f;
        float worldY = y - h;
        if (up) {
            worldY += offsetY / 2;
        } else {
            worldY -= offsetY + h;
        }
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(x, worldY);
        body = world.createBody(bodyDef);

        float r = (Math.min(texture.getRegionWidth(), texture.getRegionHeight()) / 2) * SCALE;
        CircleShape circle = new CircleShape();
        circle.setRadius(r);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        Filter filter = fixture.getFilterData();
        filter.categoryBits = 0x0001;
        filter.maskBits = BIT_SOLID | BIT_LIQUID;
        fixture.setFilterData(filter);

        width = texture.getRegionWidth() * SCALE;
        height = texture.getRegionHeight() * SCALE;

        circle.dispose();
    }
    
    @Override
    public void update(float delta) {
    }
    
    @Override
    public void render(OrthogonalTiledMapRenderer renderer) {
        float rotation = up ? 0 : 180;
        Vector2 position = body.getPosition();
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(texture,
                position.x - width / 2, position.y - height / 2, // position
                width / 2, height / 2, // origin
                width, height, // size
                1, 1, // scale
                rotation);
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
        player.stop();
        player.applyImpulse(0, sign * V);
    }

    @Override
    public Body getBody() {
        return body;
    }
}
