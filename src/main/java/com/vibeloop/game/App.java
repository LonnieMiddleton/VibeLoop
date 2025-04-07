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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
        
        // Load background image
        Image backgroundImage = null;
        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/station.jpg"));
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
        
        // Create background image view
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(primaryStage.widthProperty());
        backgroundView.fitHeightProperty().bind(primaryStage.heightProperty());
        
        // Create UI components for welcome screen
        Label welcomeLabel = new Label("Welcome to VibeLoop Game!");
        welcomeLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setEffect(new javafx.scene.effect.DropShadow(10, Color.BLACK));
        
        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("System", FontWeight.BOLD, 24));
        startButton.setPrefSize(250, 70);
        startButton.setStyle("-fx-background-color: #4287f5; -fx-text-fill: white; -fx-background-radius: 10;");
        startButton.setOnAction(e -> showCharacterSelection(primaryStage));
        
        // Create foreground content
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(welcomeLabel, startButton);
        
        // Create stack pane to layer content over background
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundView, content);
        
        // Create scene and set it to the stage
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("VibeLoop Game");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
        
        // Log available obstacles for testing
        logAvailableObstacles();
    }
    
    /**
     * Shows the character selection screen.
     */
    private void showCharacterSelection(Stage primaryStage) {
        CharacterSelectionScreen selectionScreen = new CharacterSelectionScreen(
            primaryStage, characterService, cardService, obstacleService);
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
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 