package edu.uob;

import java.util.Arrays;
import java.util.LinkedList;

public class CommandParser {
    private LinkedList<String> entities;
    private LinkedList<String> basicActions;

    public CommandParser() {
        this.entities = new LinkedList<>();
        this.basicActions = new LinkedList<>();
    }

    public LinkedList<String> getEntities() {
        return this.entities;
    }

    public LinkedList<String> getBasics() {
        return this.basicActions;
    }

    public void parseCommand(String command, GameServer server) {
        //Breaks down any entities mentioned into tokens for checking of extraneous entities
        for  (String token : Arrays.asList(command.toLowerCase().split(" "))) {
            //Checks whether token is a location or an entity within a location
            this.parseEntities(server, token);
            //Checks whether token is an artefact within player inventory or whether command contains player name
            this.parsePlayers(command, server, token);
            //Counts amount of inbuilt commands are mentioned
            this.parseBasics(token);
        }
    }

    public void parseEntities(GameServer server, String token){
        //Adds token to entities if a location name or an entity within the location and if not already in the list
        for (Location location : server.getLocations()) {
            if ((location.getName().equalsIgnoreCase(token) || location.getEntity(token) != null)
                    && !this.entities.contains(token)) {
                this.entities.add(token.toLowerCase());
            }
        }
    }

    public void parsePlayers(String command, GameServer server, String token){
        for (Player player : server.getPlayers()){
            //use of getArtefact as it also acts as a "inventoryContains" function
            if (player.getArtefact(token) != null) this.entities.add(token.toLowerCase());
            if (command.contains(player.getName())) this.entities.add(player.getName());
        }
    }

    public void parseBasics(String token){
        //if logic broken down into lines for readability
        //Token added to basicActions list only if it doesn't already exist there
        if ((token.equalsIgnoreCase("inventory") || token.equalsIgnoreCase("inv") || token.equalsIgnoreCase("goto")
                || token.equalsIgnoreCase("look") || token.equalsIgnoreCase("drop") || token.equalsIgnoreCase("get")
                || token.equalsIgnoreCase("health")) && !this.basicActions.contains(token)) {
            this.basicActions.add(token.toLowerCase());
        }
    }
}
