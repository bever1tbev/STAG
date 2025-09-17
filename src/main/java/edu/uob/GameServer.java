package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

import com.alexmerz.graphviz.objects.*;
import com.alexmerz.graphviz.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class GameServer {
    private LinkedList<Location> locations;
    private LinkedList<GameAction> actions;
    private LinkedList<Player> players;
    private Player currentPlayer;

    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        //builder split out into smaller lines for readability
        StringBuilder mainBuilder;
        mainBuilder = new StringBuilder().append("config").append(File.separator).append("extended-entities.dot");
        File entitiesFile = Paths.get(mainBuilder.toString()).toAbsolutePath().toFile();
        mainBuilder = new StringBuilder().append("config").append(File.separator).append("extended-actions.xml");
        File actionsFile = Paths.get(mainBuilder.toString()).toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        //initialises lists used to store game data
        this.locations = new LinkedList<>();
        this.actions = new LinkedList<>();
        this.players = new LinkedList<>();
        //loads in game files
        try {
            this.entityLoader(entitiesFile);
            //checks if store room exists and creates one if not
            if (this.getLocation("storeroom") == null) {
                this.locations.add(new Location("storeroom", "Storage for any entities not placed in the game"));
            }
            this.actionLoader(actionsFile);
        }catch(Exception e) {
            System.err.println(e);
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        StringBuilder stringBuilder = new StringBuilder();
        ActionHandler actionHandler = new ActionHandler();
        CommandParser commandParser = new CommandParser();
        InbuiltCommand inbuiltCommand = new InbuiltCommand();
        //Checks for lack of input or username
        if (this.checkNull(command)) return "No command or username provided";
        //Stores username then removes it from command for ease of use
        String username = Arrays.asList(command.split(":")).get(0);
        command = Arrays.asList(command.split(":")).get(1);
        //Decorative punctuation handled and whitespace added at end to deal with spaced triggers
        command = this.cleanCommand(command);
        //Sets current player returning false if the username is invalid
        if (!setCurrentPlayer(username)) return "Invalid username. Use only letters, spaces, apostrophes and hyphens";
        //Parses command and then finds any matches for further checks
        commandParser.parseCommand(command, this);
        actionHandler.findMatches(this, commandParser, command);
        //Checks for whether the relevant amount of actions has been provided
        if (actionHandler.getMatches().isEmpty() && commandParser.getBasics().isEmpty()) return "No valid action found";
        if (this.multipleActions(commandParser, actionHandler)) return "Multiple actions possible, be specific";
        //Returns a basic command or runs the action, given previous checks ensuring it can only be one or the other
        if (!commandParser.getBasics().isEmpty()) return inbuiltCommand.runCommand(this, commandParser, stringBuilder);
        else return actionHandler.performAction(this);
    }


    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println(new StringBuilder().append("Server listening on port ").append(portNumber));
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println(new StringBuilder().append("Received message from ").append(incomingCommand));
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                writer.write(new StringBuilder().append("\n").append(END_OF_TRANSMISSION).append("\n").toString());
                writer.flush();
            }
        }
    }

    public void entityLoader(File entitiesFile) throws IOException, ParseException {
        //Loads in file and then parses it to obtain relevant entity information
        FileReader fileReader = new FileReader(entitiesFile);
        Parser fileParser = new Parser();
        fileParser.parse(fileReader);
        //parts of file broken down and stored locally to reduce line length and improve readability
        Graph wholeDocument = fileParser.getGraphs().get(0);
        LinkedList<Graph> sections = new LinkedList<>(wholeDocument.getSubgraphs());
        LinkedList<Graph> locationGraphs = new LinkedList<>(sections.get(0).getSubgraphs());
        LinkedList<Edge>  paths = new LinkedList<>(sections.get(1).getEdges());
        for (Graph location : locationGraphs) {
            this.locationAdder(location);
        }
        for (Edge path : paths) {
            String tempSource = path.getSource().getNode().getId().getId();
            String tempTarget = path.getTarget().getNode().getId().getId();
            this.getLocation(tempSource).addPath(this.getLocation(tempTarget));
        }
    }

    public void locationAdder(Graph location){
        //Location objects and the entities within them added to server with names, description and entity types
        LinkedList<Graph> locationContents = new LinkedList<>(location.getSubgraphs());
        Node locationDetails = location.getNodes(false).get(0);
        String locationName = locationDetails.getId().getId();
        Location tempLocation = new Location(locationName, locationDetails.getAttribute("description"));
        for (Graph content : locationContents) {
            LinkedList<Node> tempNodes = new LinkedList<>(content.getNodes(false));
            for (Node node : tempNodes) {
                if (content.getId().getId().equalsIgnoreCase("artefacts")){
                    tempLocation.addArtefact(new Artefact(node.getId().getId(),node.getAttribute("description")));
                }
                else if (content.getId().getId().equalsIgnoreCase("characters")){
                    tempLocation.addCharacter(new GameCharacter(node.getId().getId(),node.getAttribute("description")));
                }
                else if (content.getId().getId().equalsIgnoreCase("furniture")){
                    tempLocation.addFurniture(new Furniture(node.getId().getId(), node.getAttribute("description")));
                }
            }
        }
        this.locations.add(tempLocation);
    }

    public void actionLoader(File actionsFile) throws IOException, ParserConfigurationException, SAXException {
        //Same purpose as entityLoader except for actions. Similar style methodology of breaking down into locals
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.parse(actionsFile);
        Element root = document.getDocumentElement();
        NodeList actionsList = root.getChildNodes();
        for (int i = 1; i < actionsList.getLength(); i++){
            if (i % 2 != 0){
                this.actionAdder(actionsList, i);
            }
        }
    }

    public void actionAdder(NodeList actionsList, int index){
        //Creates temporary action, filling it's contents then storing it on the server
        GameAction tempAction = new GameAction();
        NodeList childNodes = actionsList.item(index).getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++){
            NodeList nodes = childNodes.item(j).getChildNodes();
            //fillAction fills most contents of action to uphold code quality by reducing indentment.
            if (nodes.getLength() != 0) {
                this.fillAction(tempAction, nodes, childNodes, j);
            }
            if (childNodes.item(j).getNodeName().equalsIgnoreCase("narration")){
                tempAction.setNarration(childNodes.item(j).getTextContent());
            }
        }
        this.actions.add(tempAction);
    }

    public void fillAction(GameAction tempAction, NodeList nodes, NodeList childNodes, int j){
        for (int k = 0; k < nodes.getLength(); k++) {
            //if statement to prevent newlines that are present in file getting added
            if (!nodes.item(k).getTextContent().contains("\n")) {
                //each case set as one line to have less indentment and fewer lines
                switch (childNodes.item(j).getNodeName()) {
                    case "triggers": tempAction.addTrigger(nodes.item(k).getTextContent()); break;
                    case "subjects": tempAction.addSubject(nodes.item(k).getTextContent()); break;
                    case "consumed": tempAction.addConsumed(nodes.item(k).getTextContent()); break;
                    case "produced": tempAction.addProduced(nodes.item(k).getTextContent()); break;
                }
            }
        }
    }

    public boolean validName(String username){
        //going character by character of username makes if statement logic simpler
        for (int i = 0; i < username.length(); i++) {
            //if logic broken down into local variables to improve readability
            boolean isLetter = Character.isLetter(username.charAt(i));
            boolean isSpace = Character.isSpaceChar(username.charAt(i));
            boolean isApostrophe = username.charAt(i) == '\'';
            boolean isHyphen = username.charAt(i) == '-';
            if (!isLetter && !isSpace && !isApostrophe && !isHyphen){
                return false;
            }
        }
        return true;
    }

    public Player getPlayer(String playerName) {
        for (Player player : players) {
            if (player.getName().equals(playerName)) return player;
        }
        return null;
    }

    public Location getLocation(String locationName) {
        for (Location location : locations) {
            if (location.getName().equalsIgnoreCase(locationName)) return location;
        }
        return null;
    }

    public LinkedList<Location> getLocations() {
        return this.locations;
    }

    public LinkedList<GameAction> getActions() {
        return this.actions;
    }

    public LinkedList<Player> getPlayers() {
        return this.players;
    }

    public Player getCurrentPlayer(){
        return this.currentPlayer;
    }

    public boolean multipleActions(CommandParser commandParser, ActionHandler actionHandler){
        //farmed out for readability in handleCommand method
        return (!actionHandler.getMatches().isEmpty() && !commandParser.getBasics().isEmpty())
                || commandParser.getBasics().size() > 1 || actionHandler.getMatches().size() > 1;
    }

    public boolean checkNull(String command){
        //farmed out for readability in handleCommand method
        return (command == null) || !command.contains(":") || command.indexOf(":") == 0
                || command.indexOf(":") == command.length() - 1;
    }

    public boolean setCurrentPlayer(String username){
        //getPlayer acts as a "playerExists" function
        if (this.getPlayer(username) == null) {
            //checks username is valid, then creates and puts them in start location
            if (!validName(username)) return false;
            this.players.add(new Player(username, "A fellow player"));
            this.getPlayer(username).setLocation(this.locations.getFirst());
        }
        this.currentPlayer = this.getPlayer(username);
        return true;
    }

    public String cleanCommand(String command){
        //Punctuation stored as a string since arrays aren't allowed. Goes through each one replacing with space
        String punctuation = "!,.\"\'()?;:";
        for (int i = 0; i < punctuation.length(); i++) {
            command = command.replace(punctuation.charAt(i), ' ');
        }
        if (command.charAt(command.length() - 1) != ' ') {
            command = new StringBuilder().append(command).append(" ").toString();
        }
        return command;
    }
}
