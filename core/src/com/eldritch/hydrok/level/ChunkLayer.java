package com.eldritch.hydrok.level;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class ChunkLayer extends TiledMapTileLayer {
	private final Set<Body> bodies = new HashSet<Body>();
	private final World world;
	
	public ChunkLayer(World world, int width, int height, int tileWidth, int tileHeight) {
		super(width, height, tileWidth, tileHeight);
		this.world = world;
	}	
	
	public void addBody(Body body) {
		bodies.add(body);
	}
	
	public void destroy() {
		for (Body body : bodies) {
			world.destroyBody(body);
		}
	}
}
