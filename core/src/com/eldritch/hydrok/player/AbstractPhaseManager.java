package com.eldritch.hydrok.player;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player.PhaseManager;

public abstract class AbstractPhaseManager implements PhaseManager {
    private final Player player;
    private final float width;
    private final float height;
    private final float density;
    private final float restitution;
    private final short categoryBits;
    private float stateTime = 0;

    public AbstractPhaseManager(Player player, World world, int x, int y, float width, float height,
            float density, float restitution, short categoryBits) {
        // basic params
        this.player = player;
        this.width = width;
        this.height = height;
        
        // physics properties
        this.density = density;
        this.restitution = restitution;
        this.categoryBits = categoryBits;
    }
    
    @Override
    public void update(float delta, boolean grounded) {
        stateTime += delta;
        doUpdate(delta, grounded);
    }
    
    public float getStateTime() {
        return stateTime;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Body getBody() {
        return player.getBody();
    }

    @Override
    public void setActive() {
        stateTime = 0;
        
        // update all fixtures
        for (Fixture fixture : getBody().getFixtureList()) {
            // physics properties
            fixture.setDensity(density);
            fixture.setRestitution(restitution);
            
            // collision filters
            Filter filter = fixture.getFilterData();
            filter.categoryBits = categoryBits;
            filter.maskBits = 0x0001;
            fixture.setFilterData(filter);
        }
        
        // reset angular velocity
        getBody().setAngularVelocity(0);
    }
    
    protected abstract void doUpdate(float delta, boolean grounded);
}
