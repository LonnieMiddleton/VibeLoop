package com.vibeloop.game.ui;

import com.vibeloop.game.model.Character;
import com.vibeloop.game.model.Player;
import com.vibeloop.game.service.CharacterService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel for selecting a character for a player.
 */
public class CharacterSelectionPanel extends VBox {
    private final Player player;
    private final CharacterService characterService;
    private final ImageView characterImageView;
    private final Label characterNameLabel;
    private final TextField playerNameField;
    private final ProgressBar strengthBar;
    private final ProgressBar speedBar;
    private final ProgressBar techBar;
    private final ProgressBar healthBar;
    
    private String currentCharacterType;

    public CharacterSelectionPanel(Player player, CharacterService characterService) {
        this.player = player;
        this.characterService = characterService;
        this.currentCharacterType = player.getSelectedCharacter().getType();
        
        // Panel configuration
        setPadding(new Insets(15));
        setSpacing(10);
        setPrefSize(300, 400);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
        
        // Player name field
        playerNameField = new TextField(player.getName());
        playerNameField.textProperty().bindBidirectional(player.nameProperty());
        playerNameField.setMaxWidth(200);
        playerNameField.setPromptText("Enter name");
        
        // Character image view
        characterImageView = new ImageView();
        characterImageView.setFitHeight(120);
        characterImageView.setFitWidth(120);
        characterImageView.setPreserveRatio(true);
        
        // Arrow buttons for selecting characters
        Button prevButton = new Button("←");
        Button nextButton = new Button("→");
        
        prevButton.setOnAction(e -> selectPreviousCharacter());
        nextButton.setOnAction(e -> selectNextCharacter());
        
        HBox arrowButtonsBox = new HBox(10, prevButton, characterImageView, nextButton);
        arrowButtonsBox.setAlignment(Pos.CENTER);
        
        // Character name
        characterNameLabel = new Label();
        characterNameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        characterNameLabel.setTextFill(Color.WHITE);
        
        // Create stat bars
        VBox statsBox = new VBox(5);
        
        strengthBar = createStatBar("Strength");
        speedBar = createStatBar("Speed");
        techBar = createStatBar("Tech");
        healthBar = createStatBar("Health");
        
        statsBox.getChildren().addAll(
            strengthBar.getParent(),
            speedBar.getParent(),
            techBar.getParent(),
            healthBar.getParent()
        );
        
        // Add all components to the panel
        getChildren().addAll(
            playerNameField,
            arrowButtonsBox,
            characterNameLabel,
            statsBox
        );
        
        // Update the UI with the initial character
        updateCharacterDisplay();
    }
    
    /**
     * Creates a labeled stat bar for a character attribute.
     */
    private ProgressBar createStatBar(String statName) {
        Label statLabel = new Label(statName);
        statLabel.setTextFill(Color.WHITE);
        statLabel.setMinWidth(70);
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #59e287;");
        
        HBox statBox = new HBox(10, statLabel, progressBar);
        statBox.setAlignment(Pos.CENTER_LEFT);
        
        return progressBar;
    }
    
    /**
     * Updates the UI to display the current character.
     */
    private void updateCharacterDisplay() {
        Character character = characterService.getCharacter(currentCharacterType);
        player.setSelectedCharacter(character);
        
        // Update character image
        try {
            String imagePath = character.getImagePath();
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            characterImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading character image: " + e.getMessage());
        }
        
        // Update character name
        characterNameLabel.setText(character.getName());
        
        // Update stat bars (scale from 0-10 to 0-1 for progress bars)
        double maxStat = 10.0;
        strengthBar.setProgress(character.getStrength() / maxStat);
        speedBar.setProgress(character.getSpeed() / maxStat);
        techBar.setProgress(character.getTech() / maxStat);
        healthBar.setProgress(character.getHealth() / maxStat);
    }
    
    /**
     * Selects the next character in the list.
     */
    private void selectNextCharacter() {
        currentCharacterType = characterService.getNextCharacterType(currentCharacterType);
        updateCharacterDisplay();
    }
    
    /**
     * Selects the previous character in the list.
     */
    private void selectPreviousCharacter() {
        currentCharacterType = characterService.getPreviousCharacterType(currentCharacterType);
        updateCharacterDisplay();
    }
} 