package edu.uob;

import java.util.*;

public class Player extends GameEntity {
    private LinkedList<Artefact> inventory;
    private Location location;
    private int health;

    public Player(String name, String Description) {
        super(name, Description);
        this.inventory = new LinkedList<>();
        this.health = 3;
    }

    public void inventoryAdd(Artefact artefact) {
        this.inventory.add(artefact);
    }

    public void inventoryRemove(Artefact artefact) {
        this.inventory.remove(artefact);
    }

    public LinkedList<Artefact> getInventory() {
        return this.inventory;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getHealth() {
        return this.health;
    }

    public void resetHealth() {
        this.health = 3;
    }

    public void increaseHealth() {
        if (this.health != 3) this.health++;
    }

    public void decreaseHealth() {
        if (this.health != 0) this.health--;
    }

    public Artefact getArtefact(String artefactName){
        for (Artefact artefact : this.inventory){
            if (artefact.getName().equals(artefactName)) return artefact;
        }
        return null;
    }
}
