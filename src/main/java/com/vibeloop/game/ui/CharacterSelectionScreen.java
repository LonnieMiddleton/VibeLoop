package com.vibeloop.game.ui;

import com.vibeloop.game.model.Player;
import com.vibeloop.game.service.CardService;
import com.vibeloop.game.service.CharacterService;
import com.vibeloop.game.service.ObstacleService;
import com.vibeloop.game.ui.GameScreen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
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
    private final ObstacleService obstacleService;
    private final List<Player> players;
    
    public CharacterSelectionScreen(Stage stage, CharacterService characterService, CardService cardService, ObstacleService obstacleService) {
        this.stage = stage;
        this.characterService = characterService;
        this.cardService = cardService;
        this.obstacleService = obstacleService;
        this.players = new ArrayList<>();
        
        // Initialize players with default characters (including new types)
        String[] initialCharacters = {"engineer", "scientist", "pilot", "soldier"};
        for (int i = 0; i < 4; i++) {
            players.add(new Player(i + 1, characterService.getCharacter(initialCharacters[i])));
        }
    }
    
    /**
     * Shows the character selection screen.
     */
    public void show() {
        // Load background image
        Image backgroundImage = null;
        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/station.jpg"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
        
        // Create background image view
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false); // Allow stretching to fill the screen
        backgroundView.fitWidthProperty().bind(stage.widthProperty());
        backgroundView.fitHeightProperty().bind(stage.heightProperty());
        
        // Create a semi-transparent overlay to improve text readability
        javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle();
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());
        overlay.setFill(Color.rgb(0, 0, 0, 0.5)); // semi-transparent black
        
        // Create main layout
        StackPane root = new StackPane();
        
        // Create panel containing all the UI elements
        BorderPane uiPanel = new BorderPane();
        
        // Create header
        Text headerText = new Text("Select Your Characters");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 36));
        headerText.setFill(Color.WHITE);
        headerText.setEffect(new DropShadow(10, Color.BLACK)); // Add shadow for better readability
        
        StackPane header = new StackPane(headerText);
        header.setPadding(new Insets(20));
        uiPanel.setTop(header);
        
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
        
        uiPanel.setCenter(grid);
        
        // Start game button
        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("System", FontWeight.BOLD, 18));
        startButton.setPrefSize(200, 50);
        startButton.setStyle("-fx-background-color: #4287f5; -fx-text-fill: white; -fx-background-radius: 10;");
        startButton.setOnAction(e -> startGame());
        
        StackPane footer = new StackPane(startButton);
        footer.setPadding(new Insets(20));
        uiPanel.setBottom(footer);
        
        // Add all layers to the root stack pane (background at the bottom, UI on top)
        root.getChildren().addAll(backgroundView, overlay, uiPanel);
        
        // Create and display the scene
        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("VibeLoop Game - Character Selection");
        stage.setScene(scene);
        stage.setFullScreen(true);
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
        
        // Log player information (for debugging)
        System.out.println("Starting game with the following players:");
        for (Player player : players) {
            String characterType = player.getSelectedCharacter().getType();
            System.out.println(player.getName() + " as " + player.getSelectedCharacter().getName());
            System.out.println("  Starter deck: " + player.getDeck().getCards().size() + " cards");
            System.out.println("  Cards: " + player.getDeck().getCards());
        }
        
        // Create and show the game screen
        GameScreen gameScreen = new GameScreen(stage, players, obstacleService);
        gameScreen.show();
    }
} 