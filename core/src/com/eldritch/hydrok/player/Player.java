package com.eldritch.hydrok.player;

import java.util.EnumMap;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
	private static final int MAX_VELOCITY = 7;
	
	private final EnumMap<Phase, PhaseManager> managers;
	private Phase phase = Phase.Solid;

	public Player(World world) {
		managers = new EnumMap<Phase, PhaseManager>(Phase.class);
		managers.put(Phase.Solid, new SolidManager(world));
		managers.put(Phase.Liquid, new LiquidManager(world));
		managers.put(Phase.Gas, new GasManager(world));
		managers.get(phase).setActive(true);
	}

	public void update(float delta) {
		// apply right impulse, but only if max velocity is not reached yet
		Vector2 pos = getBody().getPosition();
		if (getBody().getLinearVelocity().x < MAX_VELOCITY) {
			getBody().applyLinearImpulse(0.25f, 0, pos.x, pos.y, true);
		}

		managers.get(phase).update(delta);
	}

	public void render(OrthogonalTiledMapRenderer renderer) {
		managers.get(phase).render(renderer);
	}
	
	public Vector2 getPosition() {
		return getBody().getPosition();
	}

	public Body getBody() {
		return managers.get(phase).getBody();
	}
	
	public void setPhase(Phase phase) {
		if (this.phase == phase) {
			return;
		}
		
		PhaseManager last = managers.get(this.phase);
		PhaseManager next = managers.get(phase);
		this.phase = phase;
		
		next.getBody().setTransform(last.getBody().getPosition(), last.getBody().getAngle());
		next.getBody().setLinearVelocity(last.getBody().getLinearVelocity());
		last.setActive(false);
		next.setActive(true);
	}

	public enum Phase {
		Solid, Liquid, Gas, Plasma
	}

	public static interface PhaseManager {
		void update(float delta);

		void render(OrthogonalTiledMapRenderer renderer);

		Body getBody();
		
		void setActive(boolean active);
	}
}
