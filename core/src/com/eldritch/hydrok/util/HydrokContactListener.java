package com.eldritch.hydrok.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.eldritch.hydrok.HydrokGame;
import com.eldritch.hydrok.activator.Activator;
import com.eldritch.hydrok.player.Player;

public class HydrokContactListener implements ContactListener {
    private final Player player;
    private final Map<Fixture, Integer> groundContacts = new HashMap<Fixture, Integer>();
    
    public HydrokContactListener(Player player) {
        this.player = player;
    }

    public boolean isGrounded() {
        return !groundContacts.isEmpty();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) {
            return;
        }
        
        checkBeginContact(fa);
        checkBeginContact(fb);
    }
    
    private boolean checkActivation(Object userData) {
        if (userData instanceof Activator) {
            Activator a = (Activator) userData;
            a.activate(player);
            return true;
        }
        return false;
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) {
            return;
        }

        checkEndContact(fa);
        checkEndContact(fb);
    }
    
    public void endContact(Body body) {
        for (Fixture fixture : body.getFixtureList()) {
            groundContacts.remove(fixture);
        }
    }
    
    private void checkBeginContact(Fixture fixture) {
        Object userData = fixture.getUserData();
        if (userData != null) {
            // ground
            if (userData.equals("ground")) {
                addContact(fixture);
                player.markGrounded();
            }
            
            // activator
            checkActivation(userData);
        }
    }
    
    private void checkEndContact(Fixture fixture) {
        Object userData = fixture.getUserData();
        if (userData != null) {
            // ground
            if (userData.equals("ground")) {
                removeContact(fixture);
            }
        }
    }
    
    private void addContact(Fixture fixture) {
        if (!groundContacts.containsKey(fixture)) {
            groundContacts.put(fixture, 0);
        }
        groundContacts.put(fixture, groundContacts.get(fixture) + 1);
    }
    
    private void removeContact(Fixture fixture) {
        if (!groundContacts.containsKey(fixture)) {
            return;
        }
        
        int contacts = groundContacts.get(fixture);
        if (contacts <= 1) {
            groundContacts.remove(fixture);
        } else {
            groundContacts.put(fixture, contacts - 1);
        }
    }
    
    private int getContactCount() {
        int count = 0;
        for (Entry<Fixture, Integer> entry : groundContacts.entrySet()) {
            count += entry.getValue();
        }
        return count;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
