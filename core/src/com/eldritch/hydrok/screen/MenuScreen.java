package com.eldritch.hydrok.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.hydrok.AbstractScreen;
import com.eldritch.hydrok.GameScreen;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.TutorialScreen;
import com.eldritch.hydrok.util.DefaultInputListener;

public class MenuScreen extends AbstractScreen {
	public MenuScreen(HydrokGame game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();

		// retrieve the default table actor
		Table table = super.getTable();
		
		// create heading
		LabelStyle headingStyle = new LabelStyle(getFont(), getHeadingColor());
		Label heading = new Label("Hydrok", headingStyle);
		heading.setFontScale(2);
		
		table.add(heading).spaceBottom(100);
		table.row();

		// register the button "play"
		TextButton startGameButton = new TextButton("Play", getSkin());
		startGameButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				//game.getSoundManager().play(TyrianSound.CLICK);
				game.setScreen(new GameScreen(game));
			}
		});
		table.add(startGameButton).spaceBottom(15);
		table.row();
		
		// register the button "tutorial"
        TextButton tutorialButton = new TextButton("Tutorial", getSkin());
        tutorialButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                //game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new TutorialScreen(game));
            }
        });
        table.add(tutorialButton).spaceBottom(15);
        table.row();
		
		// register quit button
		TextButton quitButton = new TextButton("Quit", getSkin());
		quitButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                //game.getSoundManager().play(TyrianSound.CLICK);
                Gdx.app.exit();
            }
        });
        table.add(quitButton);
	}
}