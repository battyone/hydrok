package com.eldritch.hydrok.entity;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public interface Entity {
    void update(float delta);
    
    void render(OrthogonalTiledMapRenderer renderer);
    
    void dispose(World world);
    
    Vector2 getPosition();
}
