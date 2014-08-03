package com.eldritch.hydrok.level;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.util.HydrokContactListener;

public class TutorialChunkGenerator extends MapChunkGenerator {
    public TutorialChunkGenerator(HydrokContactListener contactListener, TiledMap[][] chunks, World world, Randomizer rand, int width, int height) {
        super(contactListener, chunks, world, rand, width, height);
    }
}
