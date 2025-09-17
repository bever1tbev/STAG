package edu.uob;

import java.util.*;

public class Location extends GameEntity{
    private LinkedList<GameCharacter> characters;
    private LinkedList<Artefact> artefacts;
    private LinkedList<Furniture> furnitures;
    private LinkedList<Location> paths;

    public Location(String name, String description) {
        super(name, description);
        this.paths = new LinkedList<>();
        this.characters = new LinkedList<>();
        this.artefacts = new LinkedList<>();
        this.furnitures = new LinkedList<>();
    }

    public void addCharacter(GameCharacter character){
        this.characters.add(character);
    }

    public void addArtefact(Artefact artefact){
        this.artefacts.add(artefact);
    }

    public void addFurniture(Furniture furniture){
        this.furnitures.add(furniture);
    }

    public void addPath(Location path){
        this.paths.add(path);
    }

    public void removePath(Location path){
        this.paths.remove(path);
    }

    public LinkedList<GameCharacter> getCharacters() {
        return this.characters;
    }

    public LinkedList<Artefact> getArtefacts() {
        return this.artefacts;
    }

    public LinkedList<Furniture> getFurnitures() {
        return this.furnitures;
    }

    public LinkedList<Location> getPaths() {
        return this.paths;
    }

    public GameEntity getEntity(String entityName){
        //cycles through each entity list returning the entity with a matching name
        for (Artefact artefact : this.artefacts){
            if (artefact.getName().equals(entityName)) return artefact;
        }
        for (Furniture furniture : this.furnitures){
            if (furniture.getName().equals(entityName)) return furniture;
        }
        for (GameCharacter character : this.characters){
            if (character.getName().equals(entityName)) return character;
        }
        return null;
    }

    public void removeEntity(String entityName){
        //Checks if the entity exists at the location then removes it from the relevant list based on class
        if (this.getEntity(entityName) != null) {
            if (this.getEntity(entityName).getClass().equals(Artefact.class)){
                this.artefacts.remove((Artefact) this.getEntity(entityName));
            }
            else if (this.getEntity(entityName).getClass().equals(Furniture.class)){
                this.furnitures.remove((Furniture) this.getEntity(entityName));
            }
            else if (this.getEntity(entityName).getClass().equals(GameCharacter.class)){
                this.characters.remove((GameCharacter) this.getEntity(entityName));
            }
        }
    }

    public void addEntity(GameEntity entity){
        //adds entity to relevant list based on class
        if (entity.getClass().equals(Artefact.class)) this.addArtefact((Artefact) entity);
        else if (entity.getClass().equals(Furniture.class)) this.addFurniture((Furniture) entity);
        else if (entity.getClass().equals(GameCharacter.class)) this.addCharacter((GameCharacter) entity);
    }
}
