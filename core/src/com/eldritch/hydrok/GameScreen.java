package com.eldritch.hydrok;

import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.level.ProceduralTiledMap;
import com.eldritch.hydrok.level.ProceduralTiledMapRenderer;
import com.eldritch.hydrok.player.Player;
import com.eldritch.hydrok.player.Player.Phase;

public class GameScreen extends AbstractScreen {
	public static final AssetManager textureManager = new AssetManager();
	
	private ProceduralTiledMap map;
	private OrthographicCamera camera;
	private Player player;
	private World world;
	private OrthogonalTiledMapRenderer renderer;
	
	private Box2DDebugRenderer debugRenderer;
	private BitmapFont font;
	private SpriteBatch batch;

	public GameScreen(HydrokGame game) {
		super(game);
	}
	
	@Override
	public void show() {
		super.show();

		world = new World(new Vector2(0, -10), true);
		player = new Player(world, 1, 3);
		
		map = new ProceduralTiledMap(world, 10, 10);
		renderer = new ProceduralTiledMapRenderer(map, SCALE);

//		float w = Gdx.graphics.getWidth();
//		float h = Gdx.graphics.getHeight();
		float w = 30;
		float h = 20;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w, h);
		camera.zoom = 0.4f;
//		camera.zoom = 0.8f;
		camera.update();

		debugRenderer = new Box2DDebugRenderer();
		font = new BitmapFont();
		batch = new SpriteBatch();
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (Gdx.input.isKeyPressed(Keys.A)) {    
			player.setPhase(Phase.Gas);
		}
		
		if (Gdx.input.isKeyPressed(Keys.S)) {  
			player.setPhase(Phase.Solid);
		}

		if (Gdx.input.isKeyPressed(Keys.D)) {
			player.setPhase(Phase.Liquid);
		}
		
		// updates
		player.update(delta);
		map.update(player);
		
		Vector2 position = player.getPosition();
		float scale = 10 * camera.zoom / SCALE;
        camera.position.x = Math.round((position.x + 5) * scale) / scale;
        camera.position.y = Math.round(position.y * scale) / scale;
        camera.update();
		
		// set the tile map renderer view based on what the
		// camera sees and render the map
		renderer.setView(camera);
		renderer.render();
		player.render(renderer);
		
		// debug
		drawFps();
		
		debugRenderer.render(world, camera.combined);
		world.step(delta, 6, 2);
	}
	
	private void drawFps() {
	    batch.begin();
        font.draw(batch,
                "FPS: " + Gdx.graphics.getFramesPerSecond(),
                10, getHeight() - 10);
        batch.end();
	}
	
	public static TextureRegion[][] getRegions(String assetName, int w, int h) {
		return TextureRegion.split(getTexture(assetName), w, h);
	}
	
	public static Texture getTexture(String assetName) {
	    if (!textureManager.isLoaded(assetName, Texture.class)) {
            textureManager.load(assetName, Texture.class);
            textureManager.finishLoading();
        }
	    return textureManager.get(assetName, Texture.class);
	}
}
