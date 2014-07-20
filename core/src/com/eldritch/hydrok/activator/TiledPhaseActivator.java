package com.eldritch.hydrok.activator;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.hydrok.player.Player.Phase;

public abstract class TiledPhaseActivator extends PhaseActivator {
    public TiledPhaseActivator(Phase phase, TiledMapTile tile, int x, int y, Body body) {
        super(phase, tile, body, x, y);
    }
    
    public static class LiquidActivator extends TiledPhaseActivator {
        public LiquidActivator(TiledMapTile tile, int x, int y, Body body) {
            super(Phase.Liquid, tile, x, y, body);
        }
    }

    public static class SolidActivator extends TiledPhaseActivator {
        public SolidActivator(TiledMapTile tile, int x, int y, Body body) {
            super(Phase.Solid, tile, x, y, body);
        }
    }

    public static class GasActivator extends TiledPhaseActivator {
        public GasActivator(TiledMapTile tile, int x, int y, Body body) {
            super(Phase.Gas, tile, x, y, body);
        }
    }
    
    public static class PlasmaActivator extends TiledPhaseActivator {
        public PlasmaActivator(TiledMapTile tile, int x, int y, Body body) {
            super(Phase.Plasma, tile, x, y, body);
        }
    }
}
