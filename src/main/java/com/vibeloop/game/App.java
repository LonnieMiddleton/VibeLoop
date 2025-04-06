package com.vibeloop.game;

import com.vibeloop.game.service.CardService;
import com.vibeloop.game.service.CharacterService;
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

    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        characterService = new CharacterService();
        cardService = new CardService();
        
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
    }
    
    /**
     * Shows the character selection screen.
     */
    private void showCharacterSelection(Stage primaryStage) {
        CharacterSelectionScreen selectionScreen = new CharacterSelectionScreen(primaryStage, characterService, cardService);
        selectionScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 