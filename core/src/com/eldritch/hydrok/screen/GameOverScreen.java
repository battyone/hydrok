package com.eldritch.hydrok.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.eldritch.hydrok.AbstractScreen;
import com.eldritch.hydrok.GameScreen;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.util.DefaultInputListener;

public class GameOverScreen extends AbstractScreen {
    private final int distance;
    
	public GameOverScreen(HydrokGame game, int distance) {
		super(game);
		this.distance = distance;
	}

	@Override
	public void show() {
		super.show();

		// retrieve the default table actor
		Table table = super.getTable();
		table.center();
		
		LabelStyle headingStyle = new LabelStyle(getFont(), Color.WHITE);
        Label heading = new Label("Game Over!!", headingStyle);
        heading.setFontScale(2);
        
        table.add(heading).spaceBottom(75);
        table.row();
		
		table.add("Distance: " + distance).spaceBottom(75);
        table.row();

		// register the button "start game"
		TextButton startGameButton = new TextButton("Try Again?", getSkin());
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
        
        TextButton menuButton = new TextButton("Menu", getSkin());
        menuButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                //game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new MenuScreen(game));
            }
        });
        table.add(menuButton);
	}
}