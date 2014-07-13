package com.eldritch.hydrok.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.eldritch.hydrok.HydrokGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Hydrok";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new HydrokGame(), config);
	}
}
