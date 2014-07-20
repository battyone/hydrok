package com.eldritch.hydrok.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.eldritch.hydrok.activator.Activator;
import com.eldritch.hydrok.player.Player;

public class HydrokContactListener implements ContactListener {
    private final Player player;
    private final Map<Fixture, Integer> groundContacts = new HashMap<Fixture, Integer>();
    private final Map<Fixture, Integer> waterContacts = new HashMap<Fixture, Integer>();
    
    public HydrokContactListener(Player player) {
        this.player = player;
    }
    
    public boolean isWaterGrounded() {
        return !waterContacts.isEmpty();
    }

    public boolean isGrounded() {
        return !groundContacts.isEmpty();
    }
    
    public int getContactCount() {
        int count = 0;
        for (Entry<Fixture, Integer> entry : groundContacts.entrySet()) {
            count += entry.getValue();
        }
        return count;
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
                addContact(groundContacts, fixture);
                player.markGrounded();
            }
            
            // water
            if (userData.equals("water")) {
                addContact(waterContacts, fixture);
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
                removeContact(groundContacts, fixture);
            }
            
            // water
            if (userData.equals("water")) {
                removeContact(waterContacts, fixture);
            }
        }
    }
    
    private void addContact(Map<Fixture, Integer> contacts, Fixture fixture) {
        if (!contacts.containsKey(fixture)) {
            contacts.put(fixture, 0);
        }
        contacts.put(fixture, contacts.get(fixture) + 1);
    }
    
    private void removeContact(Map<Fixture, Integer> contacts, Fixture fixture) {
        if (!contacts.containsKey(fixture)) {
            return;
        }
        
        int count = contacts.get(fixture);
        if (count <= 1) {
            contacts.remove(fixture);
        } else {
            contacts.put(fixture, count - 1);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
