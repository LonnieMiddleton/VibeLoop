package com.vibeloop.game;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create UI components
        Label welcomeLabel = new Label("Hello, VibeLoop Game World!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button startButton = new Button("Start Game");
        startButton.setOnAction(e -> System.out.println("Game start button clicked!"));
        
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

    public static void main(String[] args) {
        launch(args);
    }
} 