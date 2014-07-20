package com.eldritch.hydrok.entity;

import static com.eldritch.hydrok.util.Settings.BIT_GAS;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Color;
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
import com.eldritch.hydrok.GameScreen;
import com.eldritch.hydrok.player.Player;

public class Blower implements Entity {
    private static final float V = 1.5f;
    private final Particle[] particles = new Particle[6];
    private final Body body;
    private final float width;
    private final float height;
    private final TextureRegion[][] regions;
    private final Vector2 velocity;
    
    public Blower(int x, int y, World world) {
        int d = 64;
        regions = GameScreen.getRegions("sprite/wind.png", d, d);
        velocity = randomVector2();
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KinematicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(d * SCALE);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        Filter filter = fixture.getFilterData();
        filter.categoryBits = 0x0001;
        filter.maskBits = BIT_GAS;
        fixture.setFilterData(filter);

        width = 2 * d * SCALE;
        height = 2 * d * SCALE;

        circle.dispose();
        
        // init particle effects
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
            particles[i].reset(randomRegion(regions), randomVector2());
        }
    }
    
    @Override
    public void update(float delta) {
        for (Particle particle : particles) {
            particle.update(delta);
            if (particle.isFinished()) {
                particle.reset(randomRegion(regions), randomVector2(0.2f));
            }
        }
    }
    
    @Override
    public void render(OrthogonalTiledMapRenderer renderer) {
        Vector2 position = body.getPosition();
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        for (Particle particle : particles) {
            batch.setColor(1, 1, 1, particle.alpha);
            batch.draw(particle.region,
                    position.x + particle.offset.x - width / 2,
                    position.y + particle.offset.y - height / 2, width, height);
            batch.setColor(Color.WHITE);
        }
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
        player.applyImpulse(velocity .x * V, velocity.y * SCALE);
    }

    @Override
    public Body getBody() {
        return body;
    }
    
    private class Particle {
        private final Vector2 offset = new Vector2();
        private TextureRegion region;
        private float alpha = 1;
        
        public void update(float delta) {
            float scale = delta * V * 0.5f;
            offset.add(velocity.x * scale, velocity.y * scale);
            alpha = Math.max(0, alpha - delta * 0.5f);
        }
        
        public void reset(TextureRegion region, Vector2 offset) {
            this.region = region;
            this.offset.set(offset);
            alpha = 1;
        }
        
        public boolean isFinished() {
            return offset.len2() > 2;
        }
    }
    
    private static TextureRegion randomRegion(TextureRegion[][] regions) {
        int i = (int) (Math.random() * regions.length);
        int j = (int) (Math.random() * regions[i].length);
        return regions[i][j];
    }
    
    private static Vector2 randomVector2() {
        return randomVector2(1);
    }
    
    private static Vector2 randomVector2(float scale) {
        return new Vector2((float) (Math.random() * 2 + -1), (float) (Math.random() * 2 + -1)).nor().scl(scale);
    }
}
