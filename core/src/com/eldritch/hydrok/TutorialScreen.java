package com.eldritch.hydrok;

import com.eldritch.hydrok.level.Randomizer;
import com.eldritch.hydrok.level.TutorialChunkGenerator.TutorialChunkGeneratorFactory;
import com.eldritch.hydrok.level.TutorialProgress;

public class TutorialScreen extends AbstractGameScreen {
    private final TutorialProgress progress;
    
	public TutorialScreen(HydrokGame game) {
	    this(game, new Randomizer());
	}
	
	public TutorialScreen(HydrokGame game, Randomizer randomizer) {
		this(game, randomizer, new TutorialProgress());
	}
	
	public TutorialScreen(HydrokGame game, Randomizer randomizer, TutorialProgress progress) {
        super(game, new TutorialChunkGeneratorFactory(randomizer, progress));
        this.progress = progress;
    }
	
	@Override
    protected String getLabelText() {
        return progress.getCurrentInfo();
    }
}
