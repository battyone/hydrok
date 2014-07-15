package com.eldritch.hydrok.util;

import static com.eldritch.hydrok.util.Settings.CHUNK_WIDTH;
import static com.eldritch.hydrok.util.Settings.CHUNK_HEIGHT;
import static com.eldritch.hydrok.util.Settings.CHUNKS;

public class TilePoint {
    private static final TilePoint[][] points =
            new TilePoint[CHUNK_WIDTH * CHUNKS][CHUNK_HEIGHT * CHUNKS];
    
    public final int x;
    public final int y;

    private TilePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public static TilePoint of(int x, int y) {
        if (x >= 0 && x < points.length && y >= 0 && y < points[x].length) {
            if (points[x][y] == null) {
                points[x][y] = new TilePoint(x, y);
            }
            return points[x][y];
        }
        return new TilePoint(x, y);
    }
}