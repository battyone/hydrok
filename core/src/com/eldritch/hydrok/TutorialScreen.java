package com.eldritch.hydrok;

import com.eldritch.hydrok.level.Randomizer;
import com.eldritch.hydrok.level.TutorialChunkGenerator.TutorialChunkGeneratorFactory;

public class TutorialScreen extends AbstractGameScreen {
	public TutorialScreen(HydrokGame game) {
	    this(game, new Randomizer());
	}
	
	public TutorialScreen(HydrokGame game, Randomizer randomizer) {
		super(game, new TutorialChunkGeneratorFactory(randomizer));
	}
}
