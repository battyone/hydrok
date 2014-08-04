package com.eldritch.hydrok;

import com.eldritch.hydrok.level.MapChunkGenerator.MapChunkGeneratorFactory;
import com.eldritch.hydrok.level.Randomizer;

public class GameScreen extends AbstractGameScreen {
	public GameScreen(HydrokGame game) {
	    this(game, new Randomizer());
	}
	
	public GameScreen(HydrokGame game, Randomizer randomizer) {
		super(game, new MapChunkGeneratorFactory(randomizer));
	}

    @Override
    protected String getLabelText() {
        return getDistance() + "";
    }
}
