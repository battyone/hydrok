package com.eldritch.hydrok.level;

import java.util.Random;

import com.eldritch.hydrok.HydrokGame;

public class Randomizer {
    private final Random rand = new Random();
    private float x;
    
    public void update(float x) {
        this.x = x;
    }
    
    public boolean flip(double baseProbability) {
        HydrokGame.log("base: %f dist: %f total %f", baseProbability, getDistanceBias(), baseProbability * getDistanceBias());
        return getRandom() < baseProbability * getDistanceBias();
    }
    
    public double getRandom() {
        return rand.nextDouble();
    }
    
    public double getDistanceBias() {
        return x / (500 + Math.abs(x));
    }
}
