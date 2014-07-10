package com.eldritch.hydrok.agent;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.hydrok.agent.Player.PhaseManager;

public class SolidManager implements PhaseManager {
	private final Texture texture;
	private final float width;
	private final float height;
	
	public SolidManager() {
		texture = new Texture("sprite/solid.png");
		width = SCALE * texture.getWidth();
		height = SCALE * texture.getHeight();
	}
	
	@Override
	public void render(Player player, float delta, OrthogonalTiledMapRenderer renderer) {
		Vector2 position = player.body.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(texture, position.x, position.y, width, height);
		batch.end();
	}
}
