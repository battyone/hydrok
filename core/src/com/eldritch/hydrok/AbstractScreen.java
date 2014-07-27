package com.eldritch.hydrok;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * The base class for all game screens.
 */
public abstract class AbstractScreen implements Screen {
	// the fixed viewport dimensions (ratio: 1.6)

	protected final HydrokGame game;
	protected final Stage stage;

	private BitmapFont font;
	private SpriteBatch batch;
	private Skin skin;
	private TextureAtlas atlas;
	private Table table;
	
	private int width;
	private int height;

	public AbstractScreen(HydrokGame game) {
		this.game = game;
		this.stage = new Stage();
		width = 800;
		height = 480;
		stage.setViewport(new FitViewport(width, height, stage.getViewport().getCamera()));
	}

	protected String getName() {
		return getClass().getSimpleName();
	}

	protected boolean isGameScreen() {
		return false;
	}

	// Lazily loaded collaborators

	public BitmapFont getFont() {
		if (font == null) {
		    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
		            Gdx.files.internal("skin/kenvector_future.ttf"));
		    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		    parameter.size = 44;
		    font = generator.generateFont(parameter);
		    generator.dispose();
		}
		return font;
	}

	public SpriteBatch getBatch() {
		if (batch == null) {
			batch = new SpriteBatch();
		}
		return batch;
	}

	public TextureAtlas getAtlas() {
		if (atlas == null) {
			atlas = new TextureAtlas(
					Gdx.files.internal("image-atlases/pages.atlas"));
		}
		return atlas;
	}

	protected Skin getSkin() {
		if (skin == null) {
			skin = new Skin();
			skin.add("default-font", getFont(), BitmapFont.class);
			
			FileHandle skinFile = Gdx.files.internal("skin/uiskin.json");
			FileHandle atlasFile = skinFile.sibling("uiskin.atlas");
			if (atlasFile.exists()) {
			    skin.addRegions(new TextureAtlas(atlasFile));
			}
			skin.load(skinFile);
		}
		return skin;
	}

	protected Table getTable() {
		if (table == null) {
			table = new Table(getSkin());
			table.setFillParent(true);
			table.debug();
			stage.addActor(table);
		}
		return table;
	}
	
	protected Stage getStage() {
		return stage;
	}

	// Screen implementation

	@Override
	public void show() {
		Gdx.app.log(HydrokGame.LOG, "Showing screen: " + getName());

		// set the stage as the input processor
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void resize(int width, int height) {
		Gdx.app.log(HydrokGame.LOG, "Resizing screen: " + getName() + " to: "
				+ width + " x " + height);
		this.width = width;
		this.height = height;
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render(float delta) {
		// (1) process the game logic

		// update the actors
		stage.act(delta);

		// (2) draw the result

		// clear the screen with the given RGB color (black)
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw the actors
		stage.draw();

		// draw the table debug lines
	    Table.drawDebug(stage);
	}

	@Override
	public void hide() {
		Gdx.app.log(HydrokGame.LOG, "Hiding screen: " + getName());

		// dispose the screen when leaving the screen;
		// note that the dipose() method is not called automatically by the
		// framework, so we must figure out when it's appropriate to call it
		dispose();
	}

	@Override
	public void pause() {
		Gdx.app.log(HydrokGame.LOG, "Pausing screen: " + getName());
	}

	@Override
	public void resume() {
		Gdx.app.log(HydrokGame.LOG, "Resuming screen: " + getName());
	}

	@Override
	public void dispose() {
		Gdx.app.log(HydrokGame.LOG, "Disposing screen: " + getName());

		// the following call disposes the screen's stage, but on my computer it
		// crashes the game so I commented it out; more info can be found at:
		// http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=3624
		// stage.dispose();

		// as the collaborators are lazily loaded, they may be null
		if (font != null && skin == null) {
		    // skin will dispose of the font for us
			font.dispose();
		}
		if (batch != null)
			batch.dispose();
		if (skin != null) {
			skin.dispose();
		}
		if (atlas != null)
			atlas.dispose();
	}
	
	public int getWidth() {
	    return width;
	}
	
	public int getHeight() {
	    return height;
	}
}