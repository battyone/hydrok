package com.eldritch.hydrok.player;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player.PhaseManager;

public abstract class AbstractPhaseManager implements PhaseManager {
    private final Body body;
    private final float width;
    private final float height;
    private final float density;
    private final float restitution;
    private final short categoryBits;

    public AbstractPhaseManager(Body body, World world, int x, int y, float width, float height,
            float density, float restitution, short categoryBits) {
        // basic params
        this.body = body;
        this.width = width;
        this.height = height;
        
        // physics properties
        this.density = density;
        this.restitution = restitution;
        this.categoryBits = categoryBits;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setActive() {
        // update all fixtures
        for (Fixture fixture : body.getFixtureList()) {
            // physics properties
            fixture.setDensity(density);
            fixture.setRestitution(restitution);
            
            // collision filters
            Filter filter = fixture.getFilterData();
            filter.categoryBits = categoryBits;
            filter.maskBits = 0x0001;
            fixture.setFilterData(filter);
        }
    }
}
