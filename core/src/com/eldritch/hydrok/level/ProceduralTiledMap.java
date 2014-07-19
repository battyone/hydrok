package com.eldritch.hydrok.level;

import static com.eldritch.hydrok.util.Settings.CHUNKS;
import static com.eldritch.hydrok.util.Settings.TILE_WIDTH;
import static com.eldritch.hydrok.util.Settings.TILE_HEIGHT;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.player.Player;
import com.eldritch.hydrok.util.HydrokContactListener;

public class ProceduralTiledMap extends TiledMap {
    private final TiledMap[][] chunks = new TiledMap[CHUNKS][CHUNKS];
    private final MapChunkGenerator generator;
    private final int chunkWidth;
    private final int chunkHeight;
    private final Vector2 lastPosition = new Vector2();

    private int minX = 0;
    private int minY = 0;

    public ProceduralTiledMap(HydrokContactListener listener, World world, int width, int height) {
        this.chunkWidth = width;
        this.chunkHeight = height;
        generator = new MapChunkGenerator(listener, chunks, world, width, height);

        // generate initial chunk setup: [0, 0] is bottom left
        for (int j = 0; j < CHUNKS; j++) {
            for (int i = CHUNKS - 1; i >= 0; i--) {
                chunks[i][j] = generate(i, j, 0, 0);
            }
        }

        // add index layers
        getLayers().add(new ProceduralLayer(
                0, width * CHUNKS, height * CHUNKS, TILE_WIDTH, TILE_HEIGHT));
        getLayers().add(new ProceduralLayer(
                1, width * CHUNKS, height * CHUNKS, TILE_WIDTH, TILE_HEIGHT));
    }
    
    public int getX() {
        return minX;
    }
    
    public int getY() {
        return minY;
    }
    
    public int getMinX() {
        return minX - chunkWidth / 2;
    }
    
    public int getMinY() {
        return minY - chunkHeight / 2;
    }
    
    public int getChunkWidth() {
        return chunkWidth;
    }
    
    public int getChunkHeight() {
        return chunkHeight;
    }
    
    public int getWidth() {
        return CHUNKS * chunkWidth;
    }
    
    public int getHeight() {
        return CHUNKS * chunkHeight;
    }
    
    public void update(Player player) {
        // compare current chunk with the position chunk
        int currentX = getIndex(minX, chunkWidth);
        int currentY = getIndex(minY, chunkHeight);
        
        Vector2 position = player.getPosition();
        int chunkX = getIndex(position.x, chunkWidth);
        int chunkY = getIndex(position.y, chunkHeight);
        
        float thresholdUp = minY + chunkHeight + chunkHeight / 2;
        float thresholdDown = minY - chunkHeight / 2;

        // check for horizontal crossing
        if (currentX < chunkX) {
            // right
            for (int i = CHUNKS - 1; i >= 0; i--) {
                // destroy the first column
                for (MapLayer layer : chunks[i][0].getLayers()) {
                    ChunkLayer chunk = (ChunkLayer) layer;
                    chunk.destroy();
                    generator.removeVertices(getWidth());
                }

                for (int j = 0; j < chunks[i].length - 1; j++) {
                    // shift left
                    chunks[i][j] = chunks[i][j + 1];
                }

                // regen last column
                int j = chunks.length - 1;
                chunks[i][j] = generate(i, j, chunkX, currentY);
            }

            // reset min x position
            minX = chunkX * chunkWidth;
        } else if (currentX > chunkX) {
            // left, should be rare
        } else if (position.y > thresholdUp) {
            // up
            for (int j = 0; j < chunks.length; j++) {
                // destroy the first row
                for (MapLayer layer : chunks[0][j].getLayers()) {
                    ((ChunkLayer) layer).destroy();
                }

                for (int i = 0; i < chunks.length - 1; i++) {
                    // shift down
                    chunks[i][j] = chunks[i + 1][j];
                }

                // regen last row
                int i = chunks.length - 1;
                chunks[i][j] = generate(i, j, currentX, chunkY);
            }

            // reset min y position
            minY = chunkY * chunkHeight;
        } else if (position.y < thresholdDown) {
            // down
            for (int j = 0; j < chunks.length; j++) {
                // destroy the last two rows
                for (int offset = 0; offset <= 0; offset++) {
                    for (MapLayer layer : chunks[chunks.length - 1 - offset][j].getLayers()) {
                        ((ChunkLayer) layer).destroy();
                    }
                }

                for (int i = chunks.length - 1; i >= 1; i--) {
                    // shift up
                    chunks[i][j] = chunks[i - 1][j];
                }

                // regen first two rows
                for (int offset = 0; offset <= 0; offset++) {
                    chunks[offset][j] = generate(offset, j, currentX, chunkY);
                }
            }

            // reset min y position
            minY = chunkY * chunkHeight;
        }

        // reset the last position
        lastPosition.set(position);
    }
    
    public void render(ShapeRenderer renderer) {
        renderer.begin(ShapeType.Line);
        renderer.setColor(0, 0, 1, 1);
        for (int i = 0; i < CHUNKS; i++) {
            for (int j = 0; j < CHUNKS; j++) {
                int x = minX - chunkWidth + j * chunkWidth;
                int y = minY - chunkHeight + i * chunkHeight;
                renderer.rect(x, y, chunkWidth, chunkHeight);
            }
        }
        
        renderer.setColor(1, 0, 1, 1);
        int startX = minX - chunkWidth;
        int startY = minY;
        renderer.line(startX, startY + chunkHeight + chunkHeight / 2, startX + getWidth(), startY + chunkHeight + chunkHeight / 2);
        renderer.line(startX, startY - chunkHeight / 2, startX + getWidth(), startY - chunkHeight / 2);
        
        renderer.setColor(1, 0, 0, 1);
        renderer.rect(minX - chunkWidth, minY - chunkHeight, getWidth(), getHeight());
        
        renderer.end();
    }
    
    private int getIndex(float a, int length) {
        return (int) Math.floor(a / length);
    }

    private TiledMap generate(int i, int j, int chunkX, int chunkY) {
        return generator.generate(i, j,
                (chunkX + j) * chunkWidth - chunkWidth,
                (chunkY + i) * chunkHeight - chunkHeight);
    }

    private class ProceduralLayer extends TiledMapTileLayer {
        private final int index;

        public ProceduralLayer(int index, int width, int height, int tileWidth, int tileHeight) {
            super(width, height, tileWidth, tileHeight);
            this.index = index;
        }

        @Override
        public Cell getCell(int x, int y) {
            // adjust for shifting
            x -= minX;
            y -= minY;

            x += chunkWidth;
            y += chunkHeight;

            // get relevant chunk
            int chunkX = getIndex(x, chunkWidth);
            int chunkY = getIndex(y, chunkHeight);

            // handle out of bounds
            if (chunkX < 0 || chunkY < 0 || chunkX >= CHUNKS || chunkY >= CHUNKS) {
                return null;
            }

            // check for layer existence
            if (chunks[chunkY][chunkX].getLayers().getCount() <= index) {
                return null;
            }

            // return the cell within chunk
            TiledMapTileLayer layer = (TiledMapTileLayer) chunks[chunkY][chunkX].getLayers().get(
                    index);
            int tileX = x - chunkX * chunkWidth;
            int tileY = y - chunkY * chunkHeight;
            return layer.getCell(tileX, tileY);
        }
    }
}
