package com.eldritch.hydrok.entity;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public interface Entity {
    void update(float delta);
    
    void render(OrthogonalTiledMapRenderer renderer);
}
