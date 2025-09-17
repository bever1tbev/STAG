package edu.uob;

public class InbuiltCommand {

    public InbuiltCommand(){}

    public String runCommand(GameServer server, CommandParser commandParser, StringBuilder stringBuilder){
        //Returns relevant ran command based on which inbuilt trigger has been found
        //Checks for extraneous or not enough entities before running commands
        if (commandParser.getEntities().isEmpty()){
            if (commandParser.getBasics().contains("inv") || commandParser.getBasics().contains("inventory")){
                return this.checkInventory(server, stringBuilder);
            }
            else if (commandParser.getBasics().contains("look")){
                return this.lookLocation(server, stringBuilder);
            }
            else if (commandParser.getBasics().contains("health")){
                return this.checkHealth(server, stringBuilder);
            }
        }
        else if (commandParser.getEntities().size() == 1) {
            if (commandParser.getBasics().contains("get")) {
                return this.pickupArtefact(server, commandParser, stringBuilder);
            }
            else if (commandParser.getBasics().contains("drop")) {
                return this.dropArtefact(server, commandParser, stringBuilder);
            }
            else if (commandParser.getBasics().contains("goto")) {
                return this.gotoLocation(server, commandParser, stringBuilder);
            }
        }
        return "Extraneous or too few entities. Get, drop, goto require one. Health, look, inventory require none";
    }

    public String checkInventory(GameServer server, StringBuilder stringBuilder){
        //Returns list of inventory items
        stringBuilder.append("Your inventory contains:\n");
        for (Artefact artefact : server.getCurrentPlayer().getInventory()){
            stringBuilder.append(artefact.getName()).append(" - ").append(artefact.getDescription()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String lookLocation(GameServer server, StringBuilder stringBuilder){
        //Goes through contents of location, adding to stringBuilder the name and description of each entity
        stringBuilder.append("You are currently in ")
                .append(server.getCurrentPlayer().getLocation().getDescription().toLowerCase())
                .append(" it contains:\n");
        for (Furniture furniture : server.getCurrentPlayer().getLocation().getFurnitures()){
            stringBuilder.append(furniture.getName()).append(" - ").append(furniture.getDescription()).append("\n");
        }
        for (GameCharacter character : server.getCurrentPlayer().getLocation().getCharacters()){
            stringBuilder.append(character.getName()).append(" - ").append(character.getDescription()).append("\n");
        }
        for (Artefact artefact : server.getCurrentPlayer().getLocation().getArtefacts()){
            stringBuilder.append(artefact.getName()).append(" - ").append(artefact.getDescription()).append("\n");
        }
        for (Player player : server.getPlayers()){
            if (player.getLocation().equals(server.getCurrentPlayer().getLocation())
                    && !player.getName().equals(server.getCurrentPlayer().getName())){
                stringBuilder.append(player.getName()).append(" - ").append(player.getDescription()).append("\n");
            }
        }
        stringBuilder.append("There are paths to:\n");
        for (Location path : server.getCurrentPlayer().getLocation().getPaths()){
            stringBuilder.append(path.getName()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String checkHealth(GameServer server, StringBuilder stringBuilder){
        //Checks player health and returns it
        return stringBuilder.append("Your health is ").append(server.getCurrentPlayer().getHealth()).toString();
    }

    public String pickupArtefact(GameServer server, CommandParser commandParser, StringBuilder stringBuilder){
        //Moves artefact from location to inventory, returning error if artefact not in room
        for (Artefact artefact : server.getCurrentPlayer().getLocation().getArtefacts()){
            if  (commandParser.getEntities().contains(artefact.getName().toLowerCase())){
                server.getCurrentPlayer().inventoryAdd(artefact);
                server.getCurrentPlayer().getLocation().getArtefacts().remove(artefact);
                return stringBuilder.append("You have picked up the ").append(artefact.getName()).toString();
            }
        }
        return "Specified item not in room or can't be picked up";
    }

    public String dropArtefact(GameServer server, CommandParser commandParser, StringBuilder stringBuilder){
        //Moves artefact from inventory to location, returning error if artefact not in inventory
        for (Artefact artefact : server.getCurrentPlayer().getInventory()){
            if (commandParser.getEntities().contains(artefact.getName().toLowerCase())){
                server.getCurrentPlayer().inventoryRemove(artefact);
                server.getCurrentPlayer().getLocation().getArtefacts().add(artefact);
                return stringBuilder.append("You have dropped the ").append(artefact.getName()).toString();
            }
        }
        return "You do not have the specified item in your inventory";
    }

    public String gotoLocation(GameServer server, CommandParser commandParser, StringBuilder stringBuilder){
        //Checks if there is a path to the specified location, moving player to said location if so
        for (Location path : server.getCurrentPlayer().getLocation().getPaths()){
            if (commandParser.getEntities().contains(path.getName().toLowerCase())){
                server.getCurrentPlayer().setLocation(server.getLocation(path.getName().toLowerCase()));
                return stringBuilder.append("You go to the ").append(commandParser.getEntities().getFirst()).toString();
            }
        }
        return stringBuilder.append("No path to the ").append(commandParser.getEntities().getFirst()).toString();
    }
}
