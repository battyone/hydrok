package com.eldritch.hydrok.level;

import java.util.Random;

public class Randomizer {
    private final Random rand = new Random();
    private float x;
    
    public void update(float x) {
        this.x = x;
    }
    
    public boolean flip(double baseProbability) {
        return getRandom() < baseProbability * getDistanceBias();
    }
    
    public double getRandom() {
        return rand.nextDouble();
    }
    
    public double getDistanceBias() {
        return x / (1 + Math.abs(x));
    }
}
