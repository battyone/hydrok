package com.eldritch.hydrok;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.agent.Player;

public class GameScreen extends AbstractScreen {
	private static final int MAX_VELOCITY = 7;
	
	private TiledMap map;
	private OrthographicCamera camera;
	private Player player;
	private World world;
	private OrthogonalTiledMapRenderer renderer;
	private Box2DDebugRenderer debugRenderer;

	public GameScreen(HydrokGame game) {
		super(game);
	}
	
	@Override
	public void show() {
		super.show();
		
		map = new TmxMapLoader().load("data/level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, SCALE);

//		float w = Gdx.graphics.getWidth();
//		float h = Gdx.graphics.getHeight();
		float w = 30;
		float h = 20;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w, h);
		camera.update();

		world = new World(new Vector2(0, -10), true);
		player = new Player(world);
		createBox();

		debugRenderer = new Box2DDebugRenderer();
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Vector2 vel = this.player.body.getLinearVelocity();
		Vector2 pos = this.player.body.getPosition();

		// apply left impulse, but only if max velocity is not reached yet
		if (Gdx.input.isKeyPressed(Keys.A) && vel.x > -MAX_VELOCITY) {          
		     this.player.body.applyLinearImpulse(-0.25f, 0, pos.x, pos.y, true);
		}

		// apply right impulse, but only if max velocity is not reached yet
		if (Gdx.input.isKeyPressed(Keys.D) && vel.x < MAX_VELOCITY) {
		     this.player.body.applyLinearImpulse(0.25f, 0, pos.x, pos.y, true);
		}
		
		// set the tile map renderer view based on what the
		// camera sees and render the map
		renderer.setView(camera);
		renderer.render();
		player.render(delta, renderer);
		
		debugRenderer.render(world, camera.combined);
		world.step(1 / 60f, 6, 2);
	}
	
	private void createBox() {
		// Create our body definition
		BodyDef groundBodyDef = new BodyDef();
		// Set its world position
		groundBodyDef.position.set(new Vector2(0, 0));

		// Create a body from the defintion and add it to the world
		Body groundBody = world.createBody(groundBodyDef);

		// Create a polygon shape
		PolygonShape groundBox = new PolygonShape();
		// Set the polygon shape as a box which is twice the size of our view
		// port and 20 high
		// (setAsBox takes half-width and half-height as arguments)
		groundBox.setAsBox(camera.viewportWidth, 10.0f * SCALE);
		// Create a fixture from our polygon shape and add it to our ground body
		groundBody.createFixture(groundBox, 0.0f);
		// Clean up after ourselves
		groundBox.dispose();
	}
}
