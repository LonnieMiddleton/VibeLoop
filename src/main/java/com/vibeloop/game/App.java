package com.vibeloop.game;

import com.vibeloop.game.service.CardService;
import com.vibeloop.game.service.CharacterService;
import com.vibeloop.game.service.ObstacleService;
import com.vibeloop.game.ui.CharacterSelectionScreen;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private CharacterService characterService;
    private CardService cardService;
    private ObstacleService obstacleService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        characterService = new CharacterService();
        cardService = new CardService();
        obstacleService = new ObstacleService();
        
        // Create UI components for welcome screen
        Label welcomeLabel = new Label("Welcome to VibeLoop Game!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button startButton = new Button("Start Game");
        startButton.setOnAction(e -> showCharacterSelection(primaryStage));
        
        // Create layout
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(welcomeLabel, startButton);
        
        // Create scene and set it to the stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("VibeLoop Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Log available obstacles for testing
        logAvailableObstacles();
    }
    
    /**
     * Shows the character selection screen.
     */
    private void showCharacterSelection(Stage primaryStage) {
        CharacterSelectionScreen selectionScreen = new CharacterSelectionScreen(
            primaryStage, characterService, cardService);
        selectionScreen.show();
    }
    
    /**
     * Logs available obstacles for testing.
     */
    private void logAvailableObstacles() {
        System.out.println("\nAvailable Obstacles:");
        for (var entry : obstacleService.getAllObstacleCards().entrySet()) {
            var obstacle = entry.getValue();
            System.out.println("- " + obstacle.getName() + " (Difficulty: " + obstacle.getDifficulty() + ")");
            System.out.println("  Description: " + obstacle.getDescription());
            System.out.println("  Required Skills: " + String.join(", ", obstacle.getRequiredSkills()));
            System.out.println("  Damage Per Turn: " + obstacle.getDamagePerTurn());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 