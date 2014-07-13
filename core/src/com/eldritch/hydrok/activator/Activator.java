package com.eldritch.hydrok.activator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.hydrok.player.Player;

public interface Activator {
    void activate(Player player);
    
    Body getBody();
    
    TiledMapTile getTile();
    
    int getX();
    
    int getY();
}
