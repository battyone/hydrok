package com.eldritch.hydrok.level;

import java.util.Random;

public class Randomizer {
    private final Random rand;
    private float x;
    
    public Randomizer() {
        rand = new Random();
    }
    
    public Randomizer(int seed) {
        rand = new Random(seed);
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
