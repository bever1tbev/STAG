package edu.uob;

import java.util.*;

public class ActionHandler {
    private LinkedList<GameAction> matchedActions;

    public ActionHandler(){
        this.matchedActions = new LinkedList<>();
    }

    public void findMatches(GameServer server, CommandParser commandParser, String command){
        //Goes through actions checking which triggers and subjects match based on the parsed command
        for (GameAction action : server.getActions()){
            boolean triggerMatch = false;
            Integer matchedSubjects = 0;
            for (String trigger : action.getTriggers()) {
                //StringBuilder utilised to add spaces around trigger to make sure correct spacing is used in command
                String processedTrigger = new StringBuilder()
                        .append(" ").append(trigger).append(" ")
                        .toString().toLowerCase();
                if (command.toLowerCase().contains(processedTrigger)) triggerMatch = true;
            }
            for (String subject : action.getSubjects()) {
                if (commandParser.getEntities().contains(subject.toLowerCase())) matchedSubjects++;
            }
            //checks match of trigger and subjects and availability
            if (triggerMatch && matchedSubjects > 0
                    && this.availabilityChecker(action.getSubjects(), server)
                    && matchedSubjects.equals(commandParser.getEntities().size())) {
                    this.matchedActions.add(action);
            }
        }
    }

    public boolean availabilityChecker(LinkedList<String> subjects, GameServer server){
        //checks whether subjects are either in the current player location or in the player inventory
        int availableSubjects = 0;
        if (subjects.contains("health")) availableSubjects++;
        for (String subject : subjects){
            //use of getArtefact as it also acts as a "inventoryContains" function, keeping code dry
            if (server.getCurrentPlayer().getArtefact(subject) != null
                    || server.getCurrentPlayer().getLocation().getEntity(subject) != null
                    || server.getCurrentPlayer().getLocation().getName().equals(subject)) {
                availableSubjects++;
            }
        }
        if (availableSubjects == subjects.size()) return true;
        else return false;
    }

    public String performAction(GameServer server) {
        GameAction action = this.matchedActions.getFirst();
        StringBuilder stringBuilder = new StringBuilder().append(action.getNarration());
        LinkedList<Integer> beenProduced = new LinkedList<>();
        LinkedList<Integer> beenConsumed = new LinkedList<>();
        //cycles through locations looking for consumed and produced, moving them and accounting for them accordingly
        for (Location location : server.getLocations()){
            //regular for loops used as opposed to enhanced because of issues keeping track of "been" entities
            for (int i = 0; i < action.getProduced().size(); i++){
                if (!beenProduced.contains(i) && this.moveProduced(server, action.getProduced().get(i), location)) {
                    beenProduced.add(i);
                }
            }
            for (int i = 0; i < action.getConsumed().size(); i++){
                if (!beenConsumed.contains(i) && this.moveConsumed(server, action.getConsumed().get(i), location)) {
                    beenConsumed.add(i);
                }
            }
        }
        //Checks if the player has ran out of health and handles death accordingly
        if (server.getCurrentPlayer().getHealth() == 0){
            this.handleDeath(server);
            stringBuilder.append("\nYou have died and lost all your items, you return to the start of the game!");
        }
        return stringBuilder.toString();
    }

    public boolean moveProduced(GameServer server, String entityName, Location location){
        //moves inputted entity to current location "producing" it
        if (entityName.equals("health")) server.getCurrentPlayer().increaseHealth();
        else if (location.getEntity(entityName) != null){
            server.getCurrentPlayer().getLocation().addEntity(location.getEntity(entityName));
            location.removeEntity(entityName);
        }
        else if (server.getCurrentPlayer().getArtefact(entityName) != null) {
            server.getCurrentPlayer().getLocation().addArtefact(server.getCurrentPlayer().getArtefact(entityName));
            server.getCurrentPlayer().inventoryRemove(server.getCurrentPlayer().getArtefact(entityName));
        }
        else if (entityName.equals(location.getName())) server.getCurrentPlayer().getLocation().addPath(location);
        else return false;
        return true;
    }

    public boolean moveConsumed(GameServer server, String entityName, Location location){
        //moves inputted entity to storeroom "consuming" it
        if (entityName.equals("health")) server.getCurrentPlayer().decreaseHealth();
        else if (location.getEntity(entityName) != null) {
            server.getLocation("storeroom").addEntity(location.getEntity(entityName));
            server.getCurrentPlayer().getLocation().removeEntity(entityName);
        }
        else if (server.getCurrentPlayer().getArtefact(entityName) != null) {
            server.getLocation("storeroom").addArtefact(server.getCurrentPlayer().getArtefact(entityName));
            server.getCurrentPlayer().inventoryRemove(server.getCurrentPlayer().getArtefact(entityName));
        }
        else if (entityName.equals(location.getName())) server.getCurrentPlayer().getLocation().removePath(location);
        else return false;
        return true;
    }

    public void handleDeath(GameServer server){
        //moves artefacts in player inventory to location, resets health, puts them back at start location
        for (Artefact item : server.getCurrentPlayer().getInventory()){
            server.getCurrentPlayer().getLocation().addArtefact(item);
            server.getCurrentPlayer().inventoryRemove(item);
        }
        server.getCurrentPlayer().resetHealth();
        server.getCurrentPlayer().setLocation(server.getLocations().getFirst());
    }

    public LinkedList<GameAction> getMatches(){
        return this.matchedActions;
    }
}

