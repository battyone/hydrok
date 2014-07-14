package com.eldritch.hydrok.activator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.hydrok.player.Player;
import com.eldritch.hydrok.player.Player.Phase;

public abstract class PhaseActivator implements Activator {
    private final Phase phase;
    private final TiledMapTile tile;
    private final Body body;
    private final int x;
    private final int y;

    public PhaseActivator(Phase phase, TiledMapTile tile, Body body, int x, int y) {
        this.phase = phase;
        this.x = x;
        this.y = y;
        this.tile = tile;
        this.body = body;
        for (Fixture fixture : body.getFixtureList()) {
            fixture.setUserData(this);
        }
    }

    @Override
    public void activate(Player player) {
        if (player.getPhase() != phase) {
            player.setPhase(phase);
        }
    }

    @Override
    public Body getBody() {
        return body;
    }

    public TiledMapTile getTile() {
        return tile;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
