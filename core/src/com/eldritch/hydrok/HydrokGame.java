package com.eldritch.hydrok;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.eldritch.hydrok.screen.GameOverScreen;

public class HydrokGame extends Game {
	public static final String LOG = HydrokGame.class.getSimpleName();
	public static boolean DEV_MODE = false;
	
	@Override
	public void create() {
		Gdx.app.log(HydrokGame.LOG, "Creating game on " + Gdx.app.getType());

		// // create the preferences manager preferencesManager = new
		// PreferencesManager();
		//
		// // create the music manager musicManager = new MusicManager();
		// musicManager.setVolume(preferencesManager.getVolume());
		// musicManager.setEnabled(preferencesManager.isMusicEnabled());
		//
		// // create the sound manager soundManager = new SoundManager();
		// soundManager.setVolume(preferencesManager.getVolume());
		// soundManager.setEnabled(preferencesManager.isSoundEnabled());
		//
		// // create the profile manager profileManager = new ProfileManager();
		// profileManager.retrieveProfile();
		//
		// // create the level manager levelManager = new LevelManager();
		//
		// // create the helper objects fpsLogger = new FPSLogger();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		Gdx.app.log(HydrokGame.LOG, "Resizing game to: " + width + " x " + height);

		// show the splash screen when the game is resized for the first time;
		// this approach avoids calling the screen's resize method repeatedly
		if (getScreen() == null) {
			if (DEV_MODE) {
				setScreen(new GameScreen(this));
			} else {
				setScreen(new GameOverScreen(this, 0));
			}
		}
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
		Gdx.app.log(HydrokGame.LOG, "Setting screen: "
				+ screen.getClass().getSimpleName());
	}
	
	public static void log(String text, Object... args) {
		Gdx.app.log(HydrokGame.LOG, String.format(text, args));
	}
	
	public static void error(String text, Exception ex) {
	    Gdx.app.error(HydrokGame.LOG, text, ex);
	}
}
