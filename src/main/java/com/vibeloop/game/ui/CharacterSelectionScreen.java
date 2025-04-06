package com.vibeloop.game.ui;

import com.vibeloop.game.model.Player;
import com.vibeloop.game.service.CardService;
import com.vibeloop.game.service.CharacterService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for character selection.
 */
public class CharacterSelectionScreen {
    private final Stage stage;
    private final CharacterService characterService;
    private final CardService cardService;
    private final List<Player> players;
    
    public CharacterSelectionScreen(Stage stage, CharacterService characterService, CardService cardService) {
        this.stage = stage;
        this.characterService = characterService;
        this.cardService = cardService;
        this.players = new ArrayList<>();
        
        // Initialize players with default characters
        String[] initialCharacters = {"mechanic", "medic", "pilot", "soldier"};
        for (int i = 0; i < 4; i++) {
            players.add(new Player(i + 1, characterService.getCharacter(initialCharacters[i])));
        }
    }
    
    /**
     * Shows the character selection screen.
     */
    public void show() {
        // Create main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a2a6c, #b21f1f, #fdbb2d);");
        
        // Create header
        Text headerText = new Text("Select Your Characters");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 36));
        headerText.setFill(Color.WHITE);
        
        StackPane header = new StackPane(headerText);
        header.setPadding(new Insets(20));
        root.setTop(header);
        
        // Create grid of character selection panels
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setPadding(new Insets(20));
        
        // Add character selection panel for each player
        grid.add(new CharacterSelectionPanel(players.get(0), characterService), 0, 0);
        grid.add(new CharacterSelectionPanel(players.get(1), characterService), 1, 0);
        grid.add(new CharacterSelectionPanel(players.get(2), characterService), 0, 1);
        grid.add(new CharacterSelectionPanel(players.get(3), characterService), 1, 1);
        
        root.setCenter(grid);
        
        // Start game button
        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font(18));
        startButton.setPrefSize(200, 50);
        startButton.setOnAction(e -> startGame());
        
        StackPane footer = new StackPane(startButton);
        footer.setPadding(new Insets(20));
        root.setBottom(footer);
        
        // Create and display the scene
        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("VibeLoop Game - Character Selection");
        stage.setScene(scene);
    }
    
    /**
     * Starts the game with the selected characters.
     */
    private void startGame() {
        // Initialize decks for players based on their character type
        for (Player player : players) {
            String characterType = player.getSelectedCharacter().getType();
            player.setDeck(cardService.createStarterDeck(characterType));
        }
        
        // Show game information
        System.out.println("Starting game with the following players:");
        for (Player player : players) {
            String characterType = player.getSelectedCharacter().getType();
            System.out.println(player.getName() + " as " + player.getSelectedCharacter().getName());
            System.out.println("  Starter deck: " + player.getDeck().getCards().size() + " cards");
            System.out.println("  Cards: " + player.getDeck().getCards());
        }
        
        // TO DO: Proceed to the game screen
    }
} 