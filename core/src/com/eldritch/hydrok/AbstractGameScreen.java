package com.eldritch.hydrok;

import static com.eldritch.hydrok.util.Settings.CHUNK_WIDTH;
import static com.eldritch.hydrok.util.Settings.CHUNK_HEIGHT;
import static com.eldritch.hydrok.util.Settings.SCALE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.eldritch.hydrok.activator.Terminator;
import com.eldritch.hydrok.entity.Entity;
import com.eldritch.hydrok.level.MapChunkGenerator.MapChunkGeneratorFactory;
import com.eldritch.hydrok.level.ProceduralTiledMap;
import com.eldritch.hydrok.level.ProceduralTiledMapRenderer;
import com.eldritch.hydrok.player.Player;
import com.eldritch.hydrok.player.Player.Phase;
import com.eldritch.hydrok.screen.GameOverScreen;
import com.eldritch.hydrok.util.HydrokContactListener;

public abstract class AbstractGameScreen extends AbstractScreen implements InputProcessor {
    private static final float ZOOM = 0.4f;
    private static final float DEBUG_ZOOM = 2.8f;
    
	public static final AssetManager textureManager = new AssetManager();
	
	private final List<Entity> entities = new ArrayList<Entity>();
	private final MapChunkGeneratorFactory factory;
	private ProceduralTiledMap map;
	private OrthographicCamera camera;
	private Player player;
	private World world;
	private HydrokContactListener contactListener;
	private OrthogonalTiledMapRenderer renderer;
	private Terminator terminator;
	
	private Box2DDebugRenderer debugRenderer;
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private SpriteBatch batch;
	private SpriteBatch textBatch;
	private TextureRegion bg;
	private Label distanceLabel;
	private int startX;
	private int distance;
	
	private boolean debug = false;

	public AbstractGameScreen(HydrokGame game, MapChunkGeneratorFactory factory) {
		super(game);
		this.factory = factory;
		stage.setViewport(new StretchViewport(getWidth(), getHeight()));
	}
	
	@Override
	public void show() {
		world = new World(new Vector2(0, -10), true);
		player = new Player(world, 11, 3);
		
		contactListener = player.getContactListener();
        world.setContactListener(contactListener);
		
		map = new ProceduralTiledMap(contactListener, world, factory, CHUNK_WIDTH, CHUNK_HEIGHT);
		renderer = new ProceduralTiledMapRenderer(map, SCALE);
		
		// game ends when terminator hits the player
		terminator = new Terminator(world, map, player);

		float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, (w / h) * 20, 20);
		camera.zoom = ZOOM;
		camera.update();

		debugRenderer = new Box2DDebugRenderer();
		shapeRenderer = new ShapeRenderer();
		font = getFont();
		batch = new SpriteBatch();
		textBatch = new SpriteBatch();
		bg = new TextureRegion(new Texture("background/grasslands.png"));
		startX = (int) player.getPosition().x;
		distance = 0;
		
		getTable().top();
        distanceLabel = new Label("Hydrok", new LabelStyle(getFont(), Color.WHITE));
        getTable().add(distanceLabel);
		
		Gdx.input.setInputProcessor(this);
		HydrokGame.log("start");
	}
	
	@Override
    public void resize(int width, int height) {
	    super.resize(width, height);
    }
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// draw background image
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bg,
		        camera.position.x - camera.viewportWidth / 2,
		        camera.position.y - camera.viewportHeight / 2,
		        camera.viewportWidth, camera.viewportHeight);
		batch.end();
		
		if (Gdx.input.isKeyPressed(Keys.A)) {    
			player.transition(Phase.Gas);
		}
		
		if (Gdx.input.isKeyPressed(Keys.S)) {  
			player.transition(Phase.Solid);
		}

		if (Gdx.input.isKeyPressed(Keys.D)) {
			player.transition(Phase.Liquid);
		}
		
		if (Gdx.input.isKeyPressed(Keys.F)) {
            player.transition(Phase.Plasma);
        }
		
		Vector2 pos = player.getPosition();
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
		    player.getBody().applyLinearImpulse(-0.2f, 0, pos.x, pos.y, true);
        }
		
		if (Gdx.input.isKeyPressed(Keys.UP)) {
            player.getBody().applyLinearImpulse(0, 0.2f, pos.x, pos.y, true);
        }
		
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            player.getBody().applyLinearImpulse(0.2f, 0, pos.x, pos.y, true);
        }
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            player.getBody().applyLinearImpulse(0, -0.2f, pos.x, pos.y, true);
        }
		
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
		    player.applyImpulseFrom(pos.x, pos.y - 1);
        }
		
		// bookkeeping
		Iterator<Entity> it = entities.iterator();
		while (it.hasNext()) {
		    Entity entity = it.next();
		    if (map.getOriginX() > entity.getPosition().x) {
		        entity.dispose(world);
		        it.remove();
		    }
		}
		map.addEntitiesTo(entities);
		distance = Math.max((int) player.getPosition().x - startX, distance);
		distanceLabel.setText(getLabelText());
		distanceLabel.setColor(1, getIntensity(), getIntensity(), 1);
		
		// updates
		for (Entity entity : entities) {
            entity.update(delta);
        }
		player.update(delta);
		map.update(player);
		
		// check for game over
		terminator.update(delta);
		if (terminator.isGameOver()) {
		    if (!debug) {
		        game.setScreen(new GameOverScreen(game, getDistance()));
		        return;
		    }
		}
		
		// update camera position
		Vector2 position = player.getPosition();
		float scale = 500 * camera.zoom / SCALE;
        camera.position.x = Math.round((position.x + 3) * scale) / scale;
        camera.position.y = Math.round(position.y * scale) / scale;
        camera.update();
		
		// set the tile map renderer view based on what the camera sees and render the map
        player.render(renderer);
		renderer.setView(camera);
		renderer.render();
		for (Entity entity : entities) {
            entity.render(renderer);
        }
		terminator.render(renderer);
		
		// debug
		if (debug) {
		    // render map shapes
	        shapeRenderer.setProjectionMatrix(camera.combined);
		    map.render(shapeRenderer);
		    debugRenderer.render(world, camera.combined);
		    drawFps();
		}
		
		// HUD comes last
		getStage().draw();
		
		// update physics state
		world.step(delta, 6, 2);
	}
	
	protected abstract String getLabelText();
	
	private float getIntensity() {
        return Math.min(1 - terminator.getDistancePercent() + 0.25f, 1);
    }
	
	protected int getDistance() {
	    return distance;
	}
	
	private void drawFps() {
	    textBatch.begin();
        font.draw(textBatch,
                "FPS: " + Gdx.graphics.getFramesPerSecond(),
                10, getHeight() - 10);
        textBatch.end();
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

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.Z:
                camera.zoom = camera.zoom >= DEBUG_ZOOM ? ZOOM : DEBUG_ZOOM;
                camera.update();
                return true;
            case Keys.F1:
                // debug rendering
                debug = !debug;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        player.applyImpulseFrom(world.x, world.y);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
