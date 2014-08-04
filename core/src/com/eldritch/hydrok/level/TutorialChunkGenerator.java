package com.eldritch.hydrok.level;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.level.WorldCell.Type;
import com.eldritch.hydrok.util.HydrokContactListener;

public class TutorialChunkGenerator extends MapChunkGenerator {
    private final TutorialProgress progress;
    
    public TutorialChunkGenerator(HydrokContactListener contactListener, TiledMap[][] chunks,
            World world, Randomizer rand, TutorialProgress progress, int width, int height) {
        super(contactListener, chunks, world, rand, width, height);
        this.progress = progress;
    }

    protected int doTerrainGeneration(ChunkLayer layer, int chunkI, int chunkJ, int worldX,
            int worldY) {
        int vertexCount = 0;
        for (int x2 = lastTerrain.getWorldX() - worldX + 1; x2 < layer.getWidth(); x2++) {
            int y = lastTerrain.getWorldY() - worldY;

            lastTerrain = new WorldCell(getTile("grass/mid"), x2, y, worldX + x2, worldY + y,
                    Type.Terrain);
            layer.setCell(lastTerrain.getLocalX(), lastTerrain.getLocalY(), lastTerrain);
            terrainCells.add(lastTerrain);
            vertexCount++;
            layer.updateTerrainLimit(lastTerrain.getLocalY());
        }
        return vertexCount;
    }
    
    public String getProgressText() {
        return progress.getCurrentInfo();
    }

    public static class TutorialChunkGeneratorFactory extends MapChunkGeneratorFactory {
        private final TutorialProgress progress;

        public TutorialChunkGeneratorFactory(Randomizer randomizer, TutorialProgress progress) {
            super(randomizer);
            this.progress = progress;
        }

        public TutorialChunkGenerator createGenerator(HydrokContactListener contactListener,
                TiledMap[][] chunks, World world, int width, int height) {
            return new TutorialChunkGenerator(contactListener, chunks, world, randomizer, progress,
                    width, height);
        }
    }
}
