package com.eldritch.hydrok.agent;

import java.util.EnumMap;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
	private final EnumMap<Phase, PhaseManager> managers;
	private Phase phase = Phase.Solid;
	
	public Player(World world) {
		managers = new EnumMap<Phase, PhaseManager>(Phase.class);
		managers.put(Phase.Solid, new SolidManager(world));
	}
	
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		managers.get(phase).render(this, delta, renderer);
	}
	
	public Body getBody() {
		return managers.get(phase).getBody();
	}
	
	public enum Phase {
		Solid, Liquid, Gas, Plasma
	}
	
	public static interface PhaseManager {
		void render(Player player, float delta, OrthogonalTiledMapRenderer renderer);
		
		Body getBody();
	}
}
