package com.eldritch.hydrok.level;

import java.util.Random;

public class Randomizer {
    private final Random rand;
    private final long seed;
    private float x;
    
    public Randomizer() {
        this(new Random().nextLong());
    }
    
    public Randomizer(long seed) {
        rand = new Random(seed);
        this.seed = seed;
    }
    
    public long getSeed() {
        return seed;
    }
    
    public void update(float x) {
        this.x = x;
    }
    
    public boolean fairFlip(double prob) {
        return getRandom() < prob;
    }
    
    public boolean flip(double baseProbability) {
        return getRandom() < baseProbability * getDistanceBias();
    }
    
    public double getRandom() {
        return rand.nextDouble();
    }
    
    public double getDistanceBias() {
        return x / (500 + Math.abs(x));
    }
}
