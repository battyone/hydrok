package com.eldritch.hydrok.activator;

import static com.eldritch.hydrok.util.Settings.ALL_BITS;
import static com.eldritch.hydrok.util.Settings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.hydrok.level.ProceduralTiledMap;
import com.eldritch.hydrok.player.Player;

public class Terminator implements Activator {
    private static final float V = 3.0f;
    
    private final TextureRegion region;
    private final Body body;
    private final ProceduralTiledMap map;
    private final Player player;
    private final float maxDelta;
    private boolean gameOver = false;
    
    public Terminator(World world, ProceduralTiledMap map, Player player) {
        region = new TextureRegion(new Texture("fill/terminator.png"));
        this.map = map;
        this.player = player;
        maxDelta = map.getChunkWidth() / 2;
        
        // create our body definition
        BodyDef groundBodyDef = new BodyDef();
        body = world.createBody(groundBodyDef);

        // create a polygon shape
        EdgeShape edge = new EdgeShape();
        Vector2 v1 = new Vector2(map.getMinX(), map.getMinY());
        Vector2 v2 = new Vector2(map.getMinX(), map.getMinY() + map.getHeight());
        edge.set(v1, v2);
        
        // create the fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edge;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = 0x0001;
        fixtureDef.filter.maskBits = ALL_BITS;
        body.createFixture(fixtureDef).setUserData(this);

        // clean up after ourselves
        edge.dispose();
    }
    
    public void update(float delta) {
        float nextX = body.getPosition().x + delta * getVelocity();
        nextX = Math.max(player.getPosition().x - maxDelta, nextX);
        body.setTransform(nextX, map.getMinY(), 0);
    }
    
    public float getDistancePercent() {
        float terminatorDelta = player.getPosition().x - body.getPosition().x;
        return 1 - terminatorDelta / maxDelta;
    }
    
    public void render(OrthogonalTiledMapRenderer renderer) {
        Vector2 position = body.getPosition();
        float width = region.getRegionWidth() * 3 * SCALE;
        float height = map.getHeight();
        
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(
                region,
                position.x - map.getChunkWidth() / 2 - width / 2,
                position.y - map.getChunkHeight() / 2, 
                0,
                height / 2,
                width,
                height,
                1,
                1,
                180);
        batch.draw(
                region,
                position.x - map.getChunkWidth() / 2 - width / 2,
                position.y - map.getChunkHeight() / 2, 
                width,
                height);
        batch.end();
    }
    
    /**
     * Starts at 3, scales up to 10 over time.
     */
    private float getVelocity() {
        float x = body.getPosition().x;
        float bonus = 7 * x / (5000 + Math.abs(x));
        return V + bonus;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void activate(Player player) {
        gameOver = true;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
