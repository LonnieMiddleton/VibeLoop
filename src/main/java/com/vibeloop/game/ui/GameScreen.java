package com.vibeloop.game.ui;

import com.vibeloop.game.model.Card;
import com.vibeloop.game.model.Player;
import com.vibeloop.game.model.Character;
import com.vibeloop.game.model.ObstacleCard;
import com.vibeloop.game.model.ObstacleDeck;
import com.vibeloop.game.service.ObstacleService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game screen showing player profiles, decks, discard piles, and hands.
 */
public class GameScreen {
    private final Stage stage;
    private final List<Player> players;
    private final ObstacleService obstacleService;
    private ObstacleDeck obstacleDeck;
    private ObstacleCard currentObstacle;
    private Map<Player, Card> playedCards;
    private int currentPlayerIndex;
    
    // UI constants
    private static final double PROFILE_WIDTH = 80;
    private static final double CARD_WIDTH = 60;
    private static final double CARD_HEIGHT = 63; // Reduced by ~30%
    private static final double CARD_SPACING = 3;
    
    // UI elements that need to be updated
    private VBox centerPanel;
    private Map<Player, FlowPane> playerHandPanes;
    private Map<Player, Label> playerStatusLabels;
    
    public GameScreen(Stage stage, List<Player> players, ObstacleService obstacleService) {
        this.stage = stage;
        this.players = players;
        this.obstacleService = obstacleService;
        this.obstacleDeck = obstacleService.createObstacleDeck();
        this.playedCards = new HashMap<>();
        this.playerHandPanes = new HashMap<>();
        this.playerStatusLabels = new HashMap<>();
    }
    
    /**
     * Shows the game screen with player profiles, decks, and hands.
     */
    public void show() {
        // Initialize players' hands
        initializePlayersHands();
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e3d59;");
        
        // Create left panel with player profiles and decks
        VBox playersPanel = createPlayersPanel();
        root.setLeft(playersPanel);
        
        // Create center panel for game board/obstacles
        centerPanel = new VBox(10);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setPadding(new Insets(10));
        
        root.setCenter(centerPanel);
        
        // Create scene and show
        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("VibeLoop Game");
        stage.setScene(scene);
        stage.show();
        
        // Start the game
        startGame();
    }
    
    /**
     * Initializes players' hands by drawing 3 cards for each player.
     */
    private void initializePlayersHands() {
        for (Player player : players) {
            player.getDeck().shuffle();
            player.getDeck().drawCards(3);
        }
    }
    
    /**
     * Creates the left panel with player profiles, decks, and hands.
     */
    private VBox createPlayersPanel() {
        VBox playersPanel = new VBox(10);
        playersPanel.setPadding(new Insets(10));
        playersPanel.setStyle("-fx-background-color: #0f2537;");
        playersPanel.setPrefWidth(PROFILE_WIDTH + 240);
        
        for (Player player : players) {
            VBox playerSection = createPlayerSection(player);
            playersPanel.getChildren().add(playerSection);
        }
        
        return playersPanel;
    }
    
    /**
     * Creates a section for a single player with profile, deck, discard pile, and hand.
     */
    private VBox createPlayerSection(Player player) {
        VBox playerSection = new VBox(5);
        playerSection.setPadding(new Insets(8));
        playerSection.setStyle("-fx-background-color: #2d4b6e; -fx-background-radius: 8;");
        
        // Create health bar as background for character name
        double healthPercentage = (double) player.getCurrentHealth() / player.getSelectedCharacter().getHealth();
        
        // Create health bar container
        HBox healthBarContainer = new HBox();
        healthBarContainer.setPrefHeight(24);
        healthBarContainer.setStyle("-fx-background-color: #1a3245; -fx-background-radius: 4;");
        
        // Create health bar (green portion)
        Rectangle healthBar = new Rectangle();
        healthBar.setHeight(24);
        healthBar.setWidth(healthPercentage * (PROFILE_WIDTH + 240 - 16)); // Account for padding
        
        // Set color based on health percentage
        if (healthPercentage <= 0.3) {
            healthBar.setFill(Color.rgb(200, 50, 50)); // Red
        } else if (healthPercentage <= 0.6) {
            healthBar.setFill(Color.rgb(200, 150, 50)); // Orange
        } else {
            healthBar.setFill(Color.rgb(50, 180, 50)); // Green
        }
        
        // Add health bar to container
        healthBarContainer.getChildren().add(healthBar);
        
        // Player name and character on top of health bar
        Label nameLabel = new Label(player.getName() + " - " + player.getSelectedCharacter().getName() + 
                                    " (" + player.getCurrentHealth() + "/" + player.getSelectedCharacter().getHealth() + ")");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setTranslateX(8); // Add some padding
        nameLabel.setTranslateY(-22); // Move up to overlay the health bar
        
        // Stack health bar and label
        VBox nameContainer = new VBox();
        nameContainer.getChildren().addAll(healthBarContainer, nameLabel);
        
        // Update health bar when health changes
        player.currentHealthProperty().addListener((obs, oldVal, newVal) -> {
            // Update health percentage
            double newHealthPercentage = (double) newVal.intValue() / player.getSelectedCharacter().getHealth();
            
            // Update health bar width
            healthBar.setWidth(newHealthPercentage * (PROFILE_WIDTH + 240 - 16));
            
            // Update health bar color
            if (newHealthPercentage <= 0.3) {
                healthBar.setFill(Color.rgb(200, 50, 50)); // Red
            } else if (newHealthPercentage <= 0.6) {
                healthBar.setFill(Color.rgb(200, 150, 50)); // Orange
            } else {
                healthBar.setFill(Color.rgb(50, 180, 50)); // Green
            }
            
            // Update name label
            nameLabel.setText(player.getName() + " - " + player.getSelectedCharacter().getName() + 
                             " (" + newVal + "/" + player.getSelectedCharacter().getHealth() + ")");
        });
        
        // Status label for player
        Label statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.LIGHTGREEN);
        playerStatusLabels.put(player, statusLabel);
        
        // Main row with character image and hand
        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        
        // Character image
        ImageView characterImage = new ImageView();
        characterImage.setFitWidth(PROFILE_WIDTH);
        characterImage.setFitHeight(PROFILE_WIDTH);
        characterImage.setPreserveRatio(true);
        
        // Hand of cards
        VBox handBox = new VBox(3);
        
        FlowPane handPane = new FlowPane();
        handPane.setHgap(CARD_SPACING);
        handPane.setVgap(CARD_SPACING);
        handPane.setPrefWidth(PROFILE_WIDTH + 140);
        playerHandPanes.put(player, handPane);
        
        updatePlayerHand(player);
        
        handBox.getChildren().add(handPane);
        
        try {
            // Try to load the character image
            Image image = new Image(getClass().getResourceAsStream(player.getSelectedCharacter().getImagePath()));
            characterImage.setImage(image);
        } catch (Exception e) {
            // If image can't be loaded, create a colored rectangle with the character's initial
            System.err.println("Error loading character image for " + player.getSelectedCharacter().getType() + ": " + e.getMessage());
            
            Rectangle placeholder = new Rectangle(PROFILE_WIDTH, PROFILE_WIDTH);
            
            // Different background colors for different character types
            switch (player.getSelectedCharacter().getType()) {
                case "mechanic":
                    placeholder.setFill(Color.DARKBLUE);
                    break;
                case "medic":
                    placeholder.setFill(Color.DARKGREEN);
                    break;
                case "pilot":
                    placeholder.setFill(Color.DARKGOLDENROD);
                    break;
                case "soldier":
                    placeholder.setFill(Color.DARKRED);
                    break;
                case "engineer":
                    placeholder.setFill(Color.DARKORANGE);
                    break;
                case "scientist":
                    placeholder.setFill(Color.DARKVIOLET);
                    break;
                default:
                    placeholder.setFill(Color.GRAY);
                    break;
            }
            
            placeholder.setStroke(Color.WHITE);
            placeholder.setStrokeWidth(2);
            
            // Add text with first letter of character name
            Text initial = new Text(String.valueOf(player.getSelectedCharacter().getName().charAt(0)));
            initial.setFill(Color.WHITE);
            initial.setFont(Font.font("System", FontWeight.BOLD, 36));
            
            StackPane placeholderPane = new StackPane(placeholder, initial);
            placeholderPane.setPrefSize(PROFILE_WIDTH, PROFILE_WIDTH);
            
            // Replace the ImageView with the StackPane in the layout
            characterImage = new ImageView(); // Dummy ImageView that won't be used
            mainRow.getChildren().add(placeholderPane); // Add placeholder directly
            
            // Create tooltip for character stats
            Tooltip characterTooltip = createCharacterTooltip(player.getSelectedCharacter());
            Tooltip.install(placeholderPane, characterTooltip);
            
            // Skip adding the ImageView later
            characterImage = null;
        }
        
        // Create tooltip for character stats if we have a valid image
        if (characterImage != null) {
            Tooltip characterTooltip = createCharacterTooltip(player.getSelectedCharacter());
            Tooltip.install(characterImage, characterTooltip);
            
            // Add character image and hand to main row
            mainRow.getChildren().addAll(characterImage, handBox);
        } else {
            // Character image was replaced with a placeholder above
            mainRow.getChildren().add(handBox);
        }
        
        // Deck and discard pile
        HBox deckRow = new HBox(10);
        deckRow.setAlignment(Pos.CENTER_LEFT);
        
        // Draw pile
        VBox drawPileBox = new VBox(3);
        drawPileBox.setAlignment(Pos.CENTER);
        
        try {
            Image deckBackImage = new Image(getClass().getResourceAsStream("/cards/card_back.jpg"));
            ImageView deckImage = new ImageView(deckBackImage);
            deckImage.setFitWidth(CARD_WIDTH);
            deckImage.setFitHeight(CARD_HEIGHT);
            
            Label deckLabel = new Label("Deck: " + player.getDeck().getDrawPile().size());
            deckLabel.setTextFill(Color.WHITE);
            deckLabel.setFont(Font.font("System", 11));
            
            drawPileBox.getChildren().addAll(deckImage, deckLabel);
        } catch (Exception e) {
            System.err.println("Error loading deck back image: " + e.getMessage());
        }
        
        // Discard pile
        VBox discardPileBox = new VBox(3);
        discardPileBox.setAlignment(Pos.CENTER);
        
        // Check if there's a card in the discard pile
        List<Card> discardPile = player.getDeck().getDiscardPile();
        if (!discardPile.isEmpty()) {
            Card topCard = discardPile.get(discardPile.size() - 1);
            
            // Try all possible paths for the card image
            String[] possiblePaths = {
                "/cards/" + topCard.getId() + ".jpg",
                "/cards/skills/" + topCard.getId() + ".jpg", 
                "/cards/tools/" + topCard.getId() + ".jpg"
            };
            
            boolean imageLoaded = false;
            for (String path : possiblePaths) {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    try {
                        Image cardImage = new Image(is);
                        ImageView discardImage = new ImageView(cardImage);
                        discardImage.setFitWidth(CARD_WIDTH);
                        discardImage.setFitHeight(CARD_HEIGHT);
                        discardPileBox.getChildren().add(discardImage);
                        imageLoaded = true;
                        break;
                    } catch (Exception e) {
                        continue;
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            System.err.println("Error closing stream: " + e.getMessage());
                        }
                    }
                }
            }
            
            // If no image could be loaded, use a placeholder
            if (!imageLoaded) {
                Rectangle discardPlaceholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
                discardPlaceholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
                discardPlaceholder.setStroke(Color.WHITE);
                
                // Add a label with card name inside the placeholder
                StackPane placeholderWithText = new StackPane();
                Text cardNameText = new Text(topCard.getName());
                cardNameText.setFill(Color.WHITE);
                cardNameText.setFont(Font.font("System", FontWeight.BOLD, 9));
                cardNameText.setWrappingWidth(CARD_WIDTH - 10);
                cardNameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                
                placeholderWithText.getChildren().addAll(discardPlaceholder, cardNameText);
                discardPileBox.getChildren().add(placeholderWithText);
            }
        } else {
            // No cards in discard pile, show placeholder
            Rectangle discardPlaceholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            discardPlaceholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
            discardPlaceholder.setStroke(Color.WHITE);
            discardPileBox.getChildren().add(discardPlaceholder);
        }
        
        Label discardLabel = new Label("Discard: " + player.getDeck().getDiscardPile().size());
        discardLabel.setTextFill(Color.WHITE);
        discardLabel.setFont(Font.font("System", 11));
        
        discardPileBox.getChildren().add(discardLabel);
        
        deckRow.getChildren().addAll(drawPileBox, discardPileBox);
        
        // Add all sections
        playerSection.getChildren().addAll(nameContainer, statusLabel, mainRow, deckRow);
        
        return playerSection;
    }
    
    /**
     * Updates the displayed hand for a player.
     */
    private void updatePlayerHand(Player player) {
        FlowPane handPane = playerHandPanes.get(player);
        handPane.getChildren().clear();
        
        for (Card card : player.getDeck().getHand()) {
            // Create stack pane for card with indicator
            javafx.scene.layout.StackPane cardPane = new javafx.scene.layout.StackPane();
            cardPane.setAlignment(Pos.BOTTOM_RIGHT);
            
            // Determine the associated stat and color
            String cardStat = card.getStat().toLowerCase();
            Color statColor = Color.WHITE;
            String statLetter = "";
            
            switch (cardStat) {
                case "strength":
                    statColor = Color.rgb(220, 100, 100);
                    statLetter = "S";
                    break;
                case "speed":
                    statColor = Color.rgb(100, 180, 220);
                    statLetter = "SP";
                    break;
                case "tech":
                    statColor = Color.rgb(100, 220, 100);
                    statLetter = "T";
                    break;
                default:
                    // Default to strength
                    statColor = Color.rgb(220, 100, 100);
                    statLetter = "S";
                    break;
            }
            
            // Check if card is compatible with current obstacle
            boolean isCompatible = true;
            if (currentObstacle != null) {
                isCompatible = card.isCompatibleWithType(currentObstacle.getType());
            }
            
            // Try all possible paths for the card image
            String[] possiblePaths = {
                "/cards/" + card.getId() + ".jpg",
                "/cards/skills/" + card.getId() + ".jpg", 
                "/cards/tools/" + card.getId() + ".jpg"
            };
            
            boolean imageLoaded = false;
            for (String path : possiblePaths) {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    try {
                        Image image = new Image(is);
                        ImageView cardImage = new ImageView(image);
                        cardImage.setFitWidth(CARD_WIDTH);
                        cardImage.setFitHeight(CARD_HEIGHT);
                        
                        // Add a gray overlay for incompatible cards
                        if (!isCompatible) {
                            cardImage.setEffect(new javafx.scene.effect.ColorAdjust(0, -0.5, -0.5, 0));
                        }
                        
                        // Create stat indicator
                        javafx.scene.layout.StackPane indicator = new javafx.scene.layout.StackPane();
                        Circle indicatorBg = new Circle(10);
                        indicatorBg.setFill(statColor);
                        indicatorBg.setStroke(Color.WHITE);
                        indicatorBg.setStrokeWidth(1);
                        
                        Text indicatorText = new Text(statLetter);
                        indicatorText.setFill(Color.WHITE);
                        indicatorText.setFont(Font.font("System", FontWeight.BOLD, 9));
                        
                        indicator.getChildren().addAll(indicatorBg, indicatorText);
                        
                        // Create tooltip
                        Tooltip tooltip = createCardTooltip(card);
                        tooltip.setShowDelay(Duration.millis(50));
                        Tooltip.install(cardPane, tooltip);
                        
                        // Add click handler for playing cards
                        if (currentPlayerIndex == players.indexOf(player) && currentObstacle != null) {
                            cardPane.setOnMouseClicked(event -> playCard(player, card));
                            cardPane.setStyle("-fx-cursor: hand;");
                        }
                        
                        // Add to stack pane
                        cardPane.getChildren().addAll(cardImage, indicator);
                        
                        // Add compatibility indicator if not compatible
                        if (!isCompatible) {
                            Label incompatibleLabel = new Label("!");
                            incompatibleLabel.setTextFill(Color.RED);
                            incompatibleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                            incompatibleLabel.setBackground(new javafx.scene.layout.Background(
                                new javafx.scene.layout.BackgroundFill(Color.WHITE, new javafx.scene.layout.CornerRadii(8), javafx.geometry.Insets.EMPTY)
                            ));
                            incompatibleLabel.setPadding(new Insets(0, 4, 0, 4));
                            incompatibleLabel.setTranslateX(-CARD_WIDTH/2 + 8);
                            incompatibleLabel.setTranslateY(-CARD_HEIGHT/2 + 8);
                            incompatibleLabel.setTooltip(new Tooltip("Not compatible with " + currentObstacle.getType() + " obstacles"));
                            
                            cardPane.getChildren().add(incompatibleLabel);
                        }
                        
                        handPane.getChildren().add(cardPane);
                        imageLoaded = true;
                        break;
                    } catch (Exception e) {
                        // Try next path
                        continue;
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            System.err.println("Error closing stream: " + e.getMessage());
                        }
                    }
                }
            }
            
            // If no image was loaded, use card back or placeholder with compatibility indicator
            if (!imageLoaded) {
                try {
                    InputStream backIs = getClass().getResourceAsStream("/cards/card_back.jpg");
                    if (backIs != null) {
                        Image backImage = new Image(backIs);
                        ImageView cardImage = new ImageView(backImage);
                        cardImage.setFitWidth(CARD_WIDTH);
                        cardImage.setFitHeight(CARD_HEIGHT);
                        
                        // Add a gray overlay for incompatible cards
                        if (!isCompatible) {
                            cardImage.setEffect(new javafx.scene.effect.ColorAdjust(0, -0.5, -0.5, 0));
                        }
                        
                        // Create stat indicator
                        javafx.scene.layout.StackPane indicator = new javafx.scene.layout.StackPane();
                        Circle indicatorBg = new Circle(10);
                        indicatorBg.setFill(statColor);
                        indicatorBg.setStroke(Color.WHITE);
                        indicatorBg.setStrokeWidth(1);
                        
                        Text indicatorText = new Text(statLetter);
                        indicatorText.setFill(Color.WHITE);
                        indicatorText.setFont(Font.font("System", FontWeight.BOLD, 9));
                        
                        indicator.getChildren().addAll(indicatorBg, indicatorText);
                        
                        // Add text overlay with card name
                        Text cardNameText = new Text(card.getName());
                        cardNameText.setFill(Color.WHITE);
                        cardNameText.setFont(Font.font("System", FontWeight.BOLD, 10));
                        cardNameText.setWrappingWidth(CARD_WIDTH - 10);
                        cardNameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                        
                        // Create tooltip
                        Tooltip tooltip = createCardTooltip(card);
                        tooltip.setShowDelay(Duration.millis(50));
                        Tooltip.install(cardPane, tooltip);
                        
                        // Add click handler for playing cards
                        if (currentPlayerIndex == players.indexOf(player) && currentObstacle != null) {
                            cardPane.setOnMouseClicked(event -> playCard(player, card));
                            cardPane.setStyle("-fx-cursor: hand;");
                        }
                        
                        // Add to stack pane
                        cardPane.getChildren().addAll(cardImage, cardNameText, indicator);
                        handPane.getChildren().add(cardPane);
                        
                        try {
                            backIs.close();
                        } catch (IOException e) {
                            System.err.println("Error closing stream: " + e.getMessage());
                        }
                    } else {
                        // Fall back to placeholder rectangle
                        createPlaceholderCard(card, cardPane, statColor, statLetter, player, handPane);
                    }
                } catch (Exception ex) {
                    // If card back also fails, use a placeholder rectangle
                    createPlaceholderCard(card, cardPane, statColor, statLetter, player, handPane);
                }
            }
        }
    }
    
    /**
     * Helper method to create a placeholder card with text.
     */
    private void createPlaceholderCard(Card card, StackPane cardPane, Color statColor, String statLetter, 
                                     Player player, FlowPane handPane) {
        Rectangle placeholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        placeholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
        placeholder.setStroke(Color.WHITE);
        
        // Create stat indicator
        javafx.scene.layout.StackPane indicator = new javafx.scene.layout.StackPane();
        Circle indicatorBg = new Circle(10);
        indicatorBg.setFill(statColor);
        indicatorBg.setStroke(Color.WHITE);
        indicatorBg.setStrokeWidth(1);
        
        Text indicatorText = new Text(statLetter);
        indicatorText.setFill(Color.WHITE);
        indicatorText.setFont(Font.font("System", FontWeight.BOLD, 9));
        
        indicator.getChildren().addAll(indicatorBg, indicatorText);
        
        // Add text overlay with card name
        Text cardNameText = new Text(card.getName());
        cardNameText.setFill(Color.WHITE);
        cardNameText.setFont(Font.font("System", FontWeight.BOLD, 10));
        cardNameText.setWrappingWidth(CARD_WIDTH - 10);
        cardNameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        // Create tooltip
        Tooltip tooltip = createCardTooltip(card);
        tooltip.setShowDelay(Duration.millis(50));
        Tooltip.install(cardPane, tooltip);
        
        // Add click handler for playing cards
        if (currentPlayerIndex == players.indexOf(player) && currentObstacle != null) {
            cardPane.setOnMouseClicked(event -> playCard(player, card));
            cardPane.setStyle("-fx-cursor: hand;");
        }
        
        cardPane.getChildren().addAll(placeholder, cardNameText, indicator);
        handPane.getChildren().add(cardPane);
    }
    
    /**
     * Creates a tooltip for a character with all its stats.
     */
    private Tooltip createCharacterTooltip(Character character) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(50));
        
        VBox content = new VBox(3);
        content.setPadding(new Insets(5));
        content.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        
        Text nameText = new Text(character.getName());
        nameText.setFill(Color.WHITE);
        nameText.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Text typeText = new Text("Type: " + character.getType());
        typeText.setFill(Color.WHITE);
        typeText.setFont(Font.font("System", 12));
        
        Text strengthText = new Text("Strength: " + character.getStrength());
        strengthText.setFill(Color.WHITE);
        strengthText.setFont(Font.font("System", 12));
        
        Text speedText = new Text("Speed: " + character.getSpeed());
        speedText.setFill(Color.WHITE);
        speedText.setFont(Font.font("System", 12));
        
        Text techText = new Text("Tech: " + character.getTech());
        techText.setFill(Color.WHITE);
        techText.setFont(Font.font("System", 12));
        
        Text healthText = new Text("Health: " + character.getHealth());
        healthText.setFill(Color.WHITE);
        healthText.setFont(Font.font("System", 12));
        
        Text descriptionText = new Text("Description: " + character.getDescription());
        descriptionText.setFill(Color.WHITE);
        descriptionText.setFont(Font.font("System", 12));
        descriptionText.setWrappingWidth(200);
        
        content.getChildren().addAll(nameText, typeText, strengthText, speedText, techText, healthText, descriptionText);
        
        tooltip.setGraphic(content);
        tooltip.setMaxWidth(230);
        
        return tooltip;
    }
    
    /**
     * Creates a tooltip for a card with all its details.
     */
    private Tooltip createCardTooltip(Card card) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(50));
        
        VBox content = new VBox(3);
        content.setPadding(new Insets(5));
        content.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        
        Text nameText = new Text(card.getName());
        nameText.setFill(Color.WHITE);
        nameText.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        String cardStat = card.getStat().toLowerCase();
        String associatedStat = "";
        Color statColor = Color.WHITE;
        
        switch (cardStat) {
            case "strength":
                associatedStat = "Strength";
                statColor = Color.rgb(220, 100, 100);
                break;
            case "speed":
                associatedStat = "Speed";
                statColor = Color.rgb(100, 180, 220);
                break;
            case "tech":
                associatedStat = "Tech";
                statColor = Color.rgb(100, 220, 100);
                break;
            default:
                // Default to strength for any other type
                associatedStat = "Strength";
                statColor = Color.rgb(220, 100, 100);
                break;
        }
        
        Text typeText = new Text("Stat: " + card.getStat() + " (Uses " + associatedStat + ")");
        typeText.setFill(statColor);
        typeText.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Text descriptionText = new Text("Description: " + card.getDescription());
        descriptionText.setFill(Color.WHITE);
        descriptionText.setFont(Font.font("System", 12));
        descriptionText.setWrappingWidth(250);
        
        // Add compatibility information
        String[] compatibleTypes = card.getCompatibleTypes();
        String compatibilityText = "Compatible with: ";
        if (compatibleTypes != null && compatibleTypes.length > 0) {
            compatibilityText += String.join(", ", compatibleTypes);
        } else {
            compatibilityText += "None";
        }
        
        Text compatibilityLabel = new Text(compatibilityText);
        compatibilityLabel.setFill(Color.YELLOW);
        compatibilityLabel.setFont(Font.font("System", 12));
        compatibilityLabel.setWrappingWidth(250);
        
        content.getChildren().addAll(nameText, typeText, descriptionText, compatibilityLabel);
        
        tooltip.setGraphic(content);
        tooltip.setMaxWidth(280);
        
        return tooltip;
    }
    
    /**
     * Starts the game by shuffling the obstacle deck and presenting the first obstacle.
     */
    private void startGame() {
        // Clear any existing obstacles
        obstacleDeck.resetDeck();
        
        // Shuffle the deck
        obstacleDeck.shuffle();
        
        // Start with the first player
        currentPlayerIndex = 0;
        
        // Present the first obstacle
        presentNextObstacle();
    }
    
    /**
     * Presents the next obstacle card to the players.
     */
    private void presentNextObstacle() {
        // Clear any played cards from previous round
        playedCards.clear();
        
        // Draw the next obstacle
        currentObstacle = obstacleDeck.drawObstacle();
        
        if (currentObstacle == null) {
            // No more obstacles, game is over with success
            showGameResult("Victory! All obstacles have been overcome!");
            return;
        }
        
        // Reset player status
        for (Player player : players) {
            playerStatusLabels.get(player).setText("Waiting");
            playerStatusLabels.get(player).setTextFill(Color.YELLOW);
        }
        
        // Set first player as active
        currentPlayerIndex = 0;
        playerStatusLabels.get(players.get(currentPlayerIndex)).setText("Your Turn");
        playerStatusLabels.get(players.get(currentPlayerIndex)).setTextFill(Color.LIGHTGREEN);
        
        // Update all player hands to show clickable cards for the active player
        for (Player player : players) {
            updatePlayerHand(player);
        }
        
        // Create the obstacle display
        updateObstacleDisplay();
    }
    
    /**
     * Updates the center panel to display the current obstacle.
     */
    private void updateObstacleDisplay() {
        centerPanel.getChildren().clear();
        
        // Obstacle title
        Label titleLabel = new Label("Current Obstacle");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);
        
        // Obstacle card display
        VBox obstacleBox = new VBox(10);
        obstacleBox.setAlignment(Pos.CENTER);
        obstacleBox.setPadding(new Insets(10));
        obstacleBox.setStyle("-fx-background-color: #2d4b6e; -fx-background-radius: 8;");
        
        // Obstacle image
        ImageView obstacleImage;
        try {
            Image image = new Image(getClass().getResourceAsStream(currentObstacle.getImagePath()));
            obstacleImage = new ImageView(image);
            obstacleImage.setFitWidth(CARD_WIDTH * 2);
            obstacleImage.setFitHeight(CARD_HEIGHT * 2);
            obstacleBox.getChildren().add(obstacleImage);
        } catch (Exception e) {
            // If image can't be loaded, use a placeholder
            Rectangle placeholder = new Rectangle(CARD_WIDTH * 2, CARD_HEIGHT * 2);
            placeholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
            placeholder.setStroke(Color.WHITE);
            obstacleBox.getChildren().add(placeholder);
            System.err.println("Error loading obstacle image: " + e.getMessage());
        }
        
        // Obstacle details
        Label nameLabel = new Label(currentObstacle.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);
        
        Label difficultyLabel = new Label("Difficulty: " + currentObstacle.getDifficulty());
        difficultyLabel.setFont(Font.font("System", 14));
        difficultyLabel.setTextFill(Color.WHITE);
        
        // Add obstacle type with a color based on the type
        Label typeLabel = new Label("Type: " + currentObstacle.getType());
        typeLabel.setFont(Font.font("System", 14));
        
        // Set color based on type
        String type = currentObstacle.getType().toLowerCase();
        switch (type) {
            case "barrier":
                typeLabel.setTextFill(Color.ORANGE);
                break;
            case "hazard":
                typeLabel.setTextFill(Color.RED);
                break;
            case "environment":
                typeLabel.setTextFill(Color.LIGHTBLUE);
                break;
            case "personnel":
                typeLabel.setTextFill(Color.LIGHTGREEN);
                break;
            default:
                typeLabel.setTextFill(Color.WHITE);
                break;
        }
        
        Label skillsLabel = new Label("Required Skills: " + String.join(", ", currentObstacle.getRequiredSkills()));
        skillsLabel.setFont(Font.font("System", 14));
        skillsLabel.setTextFill(Color.WHITE);
        
        Label descriptionLabel = new Label(currentObstacle.getDescription());
        descriptionLabel.setFont(Font.font("System", 14));
        descriptionLabel.setTextFill(Color.WHITE);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(300);
        
        obstacleBox.getChildren().addAll(nameLabel, difficultyLabel, typeLabel, skillsLabel, descriptionLabel);
        
        // Add played cards section
        VBox playedCardsBox = new VBox(5);
        playedCardsBox.setAlignment(Pos.CENTER);
        
        Label playedCardsLabel = new Label("Played Cards");
        playedCardsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        playedCardsLabel.setTextFill(Color.WHITE);
        
        FlowPane playedCardsPane = new FlowPane();
        playedCardsPane.setHgap(CARD_SPACING * 2);
        playedCardsPane.setVgap(CARD_SPACING * 2);
        playedCardsPane.setAlignment(Pos.CENTER);
        
        // Add played cards if any
        for (Map.Entry<Player, Card> entry : playedCards.entrySet()) {
            Player player = entry.getKey();
            Card card = entry.getValue();
            
            VBox cardBox = new VBox(2);
            cardBox.setAlignment(Pos.CENTER);
            
            // Card image or "Skipped" indicator
            if (card != null) {
                // Determine the associated stat and color
                String cardStat = card.getStat().toLowerCase();
                Color statColor = Color.WHITE;
                String statLetter = "";
                
                switch (cardStat) {
                    case "strength":
                        statColor = Color.rgb(220, 100, 100);
                        statLetter = "S";
                        break;
                    case "speed":
                        statColor = Color.rgb(100, 180, 220);
                        statLetter = "SP";
                        break;
                    case "tech":
                        statColor = Color.rgb(100, 220, 100);
                        statLetter = "T";
                        break;
                    default:
                        // Default to strength
                        statColor = Color.rgb(220, 100, 100);
                        statLetter = "S";
                        break;
                }
                
                // Card image
                javafx.scene.layout.StackPane cardPane = new javafx.scene.layout.StackPane();
                cardPane.setAlignment(Pos.BOTTOM_RIGHT);
                
                // Try all possible paths for the card image
                String[] possiblePaths = {
                    "/cards/" + card.getId() + ".jpg",
                    "/cards/skills/" + card.getId() + ".jpg", 
                    "/cards/tools/" + card.getId() + ".jpg"
                };
                
                boolean imageLoaded = false;
                for (String path : possiblePaths) {
                    try {
                        Image image = new Image(getClass().getResourceAsStream(path));
                        ImageView cardImage = new ImageView(image);
                        cardImage.setFitWidth(CARD_WIDTH * 1.5);
                        cardImage.setFitHeight(CARD_HEIGHT * 1.5);
                        
                        // Create stat indicator
                        javafx.scene.layout.StackPane indicator = new javafx.scene.layout.StackPane();
                        Circle indicatorBg = new Circle(15); // Larger for better visibility
                        indicatorBg.setFill(statColor);
                        indicatorBg.setStroke(Color.WHITE);
                        indicatorBg.setStrokeWidth(1.5);
                        
                        Text indicatorText = new Text(statLetter);
                        indicatorText.setFill(Color.WHITE);
                        indicatorText.setFont(Font.font("System", FontWeight.BOLD, 12));
                        
                        indicator.getChildren().addAll(indicatorBg, indicatorText);
                        
                        // Create tooltip
                        Tooltip tooltip = createCardTooltip(card);
                        Tooltip.install(cardPane, tooltip);
                        
                        // Add to stack pane
                        cardPane.getChildren().addAll(cardImage, indicator);
                        imageLoaded = true;
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
                
                if (!imageLoaded) {
                    Rectangle placeholder = new Rectangle(CARD_WIDTH * 1.5, CARD_HEIGHT * 1.5);
                    placeholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
                    placeholder.setStroke(Color.WHITE);
                    
                    // Create stat indicator
                    javafx.scene.layout.StackPane indicator = new javafx.scene.layout.StackPane();
                    Circle indicatorBg = new Circle(15);
                    indicatorBg.setFill(statColor);
                    indicatorBg.setStroke(Color.WHITE);
                    indicatorBg.setStrokeWidth(1.5);
                    
                    Text indicatorText = new Text(statLetter);
                    indicatorText.setFill(Color.WHITE);
                    indicatorText.setFont(Font.font("System", FontWeight.BOLD, 12));
                    
                    indicator.getChildren().addAll(indicatorBg, indicatorText);
                    
                    cardPane.getChildren().addAll(placeholder, indicator);
                }
                
                cardBox.getChildren().add(cardPane);
            } else {
                // Skipped indicator
                Rectangle skipRect = new Rectangle(CARD_WIDTH * 1.5, CARD_HEIGHT * 1.5);
                skipRect.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.2));
                skipRect.setStroke(Color.WHITE);
                
                Text skipText = new Text("SKIPPED");
                skipText.setFill(Color.WHITE);
                skipText.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                // Stack the text on top of the rectangle
                javafx.scene.layout.StackPane skipPane = new javafx.scene.layout.StackPane();
                skipPane.getChildren().addAll(skipRect, skipText);
                
                cardBox.getChildren().add(skipPane);
            }
            
            // Player name
            Label playerLabel = new Label(player.getName());
            playerLabel.setTextFill(Color.WHITE);
            cardBox.getChildren().add(playerLabel);
            
            playedCardsPane.getChildren().add(cardBox);
        }
        
        playedCardsBox.getChildren().addAll(playedCardsLabel, playedCardsPane);
        
        // Add "Skip" button for current player
        Button skipButton = new Button("Skip (Play No Card)");
        skipButton.setOnAction(event -> skipTurn(players.get(currentPlayerIndex)));
        
        // Add everything to the center panel
        centerPanel.getChildren().addAll(titleLabel, obstacleBox, playedCardsBox, skipButton);
    }
    
    /**
     * Handles a player playing a card.
     */
    private void playCard(Player player, Card card) {
        if (currentPlayerIndex != players.indexOf(player)) {
            return; // Not this player's turn
        }
        
        // Play the card
        player.getDeck().playCard(card);
        playedCards.put(player, card);
        
        // Draw a new card to maintain 3 cards in hand if possible
        if (player.getDeck().getHand().size() < 3) {
            player.getDeck().drawCard();
        }
        
        // Update the display to show deck and discard pile changes
        updatePlayerUI(player);
        
        // Move to next player
        advanceToNextPlayer();
    }
    
    /**
     * Updates the UI elements for a specific player (hand, deck counts, etc.)
     */
    private void updatePlayerUI(Player player) {
        // Update hand display
        updatePlayerHand(player);
        
        // Find deck and discard pile containers
        VBox playerSection = (VBox) playerHandPanes.get(player).getParent().getParent().getParent();
        HBox deckRow = (HBox) playerSection.getChildren().get(playerSection.getChildren().size() - 1);
        
        // Update deck count
        VBox drawPileBox = (VBox) deckRow.getChildren().get(0);
        Label deckLabel = (Label) drawPileBox.getChildren().get(1);
        deckLabel.setText("Deck: " + player.getDeck().getDrawPile().size());
        
        // Update discard pile visual and count
        VBox discardPileBox = (VBox) deckRow.getChildren().get(1);
        discardPileBox.getChildren().clear();
        
        // Check if there's a card in the discard pile
        List<Card> discardPile = player.getDeck().getDiscardPile();
        if (!discardPile.isEmpty()) {
            Card topCard = discardPile.get(discardPile.size() - 1);
            System.out.println("Discard pile for " + player.getName() + " has " + discardPile.size() + " cards. Top card: " + topCard.getName());
            
            // Try all possible paths for the card image
            String[] possiblePaths = {
                "/cards/" + topCard.getId() + ".jpg",
                "/cards/skills/" + topCard.getId() + ".jpg", 
                "/cards/tools/" + topCard.getId() + ".jpg"
            };
            
            boolean imageLoaded = false;
            for (String path : possiblePaths) {
                System.out.println("Trying path: " + path);
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    try {
                        Image cardImage = new Image(is);
                        ImageView discardImage = new ImageView(cardImage);
                        discardImage.setFitWidth(CARD_WIDTH);
                        discardImage.setFitHeight(CARD_HEIGHT);
                        discardPileBox.getChildren().add(discardImage);
                        System.out.println("Successfully loaded image from: " + path);
                        imageLoaded = true;
                        break;
                    } catch (Exception e) {
                        System.err.println("Error loading image from " + path + ": " + e.getMessage());
                        continue;
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            System.err.println("Error closing stream: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Resource not found: " + path);
                }
            }
            
            // If no image could be loaded, use a placeholder
            if (!imageLoaded) {
                System.out.println("Using placeholder for " + topCard.getName());
                Rectangle discardPlaceholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
                discardPlaceholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
                discardPlaceholder.setStroke(Color.WHITE);
                
                // Add a label with card name inside the placeholder
                StackPane placeholderWithText = new StackPane();
                Text cardNameText = new Text(topCard.getName());
                cardNameText.setFill(Color.WHITE);
                cardNameText.setFont(Font.font("System", FontWeight.BOLD, 9));
                cardNameText.setWrappingWidth(CARD_WIDTH - 10);
                cardNameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                
                placeholderWithText.getChildren().addAll(discardPlaceholder, cardNameText);
                discardPileBox.getChildren().add(placeholderWithText);
            }
        } else {
            // No cards in discard pile, show placeholder
            Rectangle discardPlaceholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            discardPlaceholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
            discardPlaceholder.setStroke(Color.WHITE);
            discardPileBox.getChildren().add(discardPlaceholder);
        }
        
        // Add discard count label
        Label discardLabel = new Label("Discard: " + player.getDeck().getDiscardPile().size());
        discardLabel.setTextFill(Color.WHITE);
        discardLabel.setFont(Font.font("System", 11));
        discardPileBox.getChildren().add(discardLabel);
    }
    
    /**
     * Handles a player skipping their turn.
     */
    private void skipTurn(Player player) {
        if (currentPlayerIndex != players.indexOf(player)) {
            return; // Not this player's turn
        }
        
        // Mark as skipped (no card played)
        playedCards.put(player, null);
        
        // Draw a new card to maintain 3 cards in hand if possible
        if (player.getDeck().getHand().size() < 3) {
            player.getDeck().drawCard();
        }
        
        // Update the display
        updatePlayerUI(player);
        
        // Move to next player
        advanceToNextPlayer();
    }
    
    /**
     * Advances to the next player or resolves the obstacle if all players have taken their turn.
     */
    private void advanceToNextPlayer() {
        // Update current player status to "Done"
        playerStatusLabels.get(players.get(currentPlayerIndex)).setText("Done");
        playerStatusLabels.get(players.get(currentPlayerIndex)).setTextFill(Color.LIGHTBLUE);
        
        // Move to next player
        currentPlayerIndex++;
        
        // Check if all players have played
        if (currentPlayerIndex >= players.size()) {
            // All players have played, resolve the obstacle
            resolveObstacle();
        } else {
            // Update next player status
            playerStatusLabels.get(players.get(currentPlayerIndex)).setText("Your Turn");
            playerStatusLabels.get(players.get(currentPlayerIndex)).setTextFill(Color.LIGHTGREEN);
            
            // Update player hands to show clickable cards for current player
            for (Player player : players) {
                updatePlayerHand(player);
            }
            
            // Update the obstacle display
            updateObstacleDisplay();
        }
    }
    
    /**
     * Resolves the current obstacle by calculating total skill vs. difficulty.
     */
    private void resolveObstacle() {
        // Calculate total skill against the obstacle
        int totalSkill = 0;
        String[] requiredSkills = currentObstacle.getRequiredSkills();
        StringBuilder skillBreakdown = new StringBuilder();
        
        // For each player who played a card, check if the card type matches a required skill
        for (Map.Entry<Player, Card> entry : playedCards.entrySet()) {
            Player player = entry.getKey();
            Card card = entry.getValue();
            Character character = player.getSelectedCharacter();
            
            if (card != null) {
                // Check if the card is compatible with the obstacle type
                boolean isCompatible = card.isCompatibleWithType(currentObstacle.getType());
                
                // If card is not compatible with obstacle type, it contributes nothing
                if (!isCompatible) {
                    skillBreakdown.append(player.getName()).append(": ").append(card.getName())
                        .append(" (0 - INCOMPATIBLE: Not usable against ").append(currentObstacle.getType()).append(")\n");
                    continue; // Skip to next player
                }
                
                // Check if the card type matches a required skill
                String cardStat = card.getStat().toLowerCase();
                boolean matchesSkill = false;
                
                for (String skill : requiredSkills) {
                    if (cardStat.equals(skill.toLowerCase())) {
                        matchesSkill = true;
                        break;
                    }
                }
                
                int contributedSkill = 0;
                
                if (matchesSkill) {
                    // Card type matches a required skill
                    switch (cardStat) {
                        case "strength":
                            contributedSkill = character.getStrength();
                            break;
                        case "speed":
                            contributedSkill = character.getSpeed();
                            break;
                        case "tech":
                            contributedSkill = character.getTech();
                            break;
                        default:
                            // Default to strength for any other type
                            contributedSkill = character.getStrength();
                            break;
                    }
                    
                    skillBreakdown.append(player.getName()).append(": ").append(card.getName())
                        .append(" (").append(cardStat).append(" ").append(contributedSkill).append(")\n");
                } else {
                    // Card doesn't match a required skill, contributes base value of 1
                    contributedSkill = 1;
                    skillBreakdown.append(player.getName()).append(": ").append(card.getName())
                        .append(" (Non-matching - base value 1)\n");
                }
                
                totalSkill += contributedSkill;
            } else {
                // Player skipped
                skillBreakdown.append(player.getName()).append(": Skipped (0)\n");
            }
        }
        
        // Compare total skill to obstacle difficulty
        int obstacleDifficulty = currentObstacle.getDifficulty();
        
        if (totalSkill >= obstacleDifficulty) {
            // Success! Players overcome the obstacle
            obstacleDeck.defeatObstacle(currentObstacle);
            showObstacleResult("Success! Obstacle Overcome", 
                              "Total Skill: " + totalSkill + " vs. Difficulty: " + obstacleDifficulty + "\n\n" +
                              skillBreakdown.toString(),
                              true);
        } else {
            // Failure - players take damage
            int damage = obstacleDifficulty - totalSkill;
            distributeAndApplyDamage(damage);
            
            showObstacleResult("Failure! Obstacle Not Overcome", 
                              "Total Skill: " + totalSkill + " vs. Difficulty: " + obstacleDifficulty + 
                              "\nPlayers Take " + damage + " Damage\n\n" +
                              skillBreakdown.toString(),
                              false);
        }
    }
    
    /**
     * Distributes and applies damage to players.
     */
    private void distributeAndApplyDamage(int totalDamage) {
        // Simple distribution - divide damage equally among players
        int playersCount = players.size();
        int damagePerPlayer = totalDamage / playersCount;
        int remainingDamage = totalDamage % playersCount;
        
        boolean allDefeated = true;
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            
            // Calculate damage for this player
            int playerDamage = damagePerPlayer;
            if (i < remainingDamage) {
                playerDamage += 1; // Distribute remaining damage
            }
            
            // Apply damage to the player
            int remainingHealth = player.takeDamage(playerDamage);
            System.out.println(player.getName() + " takes " + playerDamage + " damage, health: " + remainingHealth);
            
            if (!player.isDefeated()) {
                allDefeated = false;
            }
        }
        
        // Check if all players are defeated
        if (allDefeated) {
            // Game over - all players are defeated
            showGameResult("Game Over! All players have been defeated!");
        }
    }
    
    /**
     * Shows the result of the obstacle resolution.
     */
    private void showObstacleResult(String title, String message, boolean success) {
        centerPanel.getChildren().clear();
        
        VBox resultBox = new VBox(10);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(20));
        
        if (success) {
            resultBox.setStyle("-fx-background-color: #2d6e4a; -fx-background-radius: 8;");
        } else {
            resultBox.setStyle("-fx-background-color: #6e2d2d; -fx-background-radius: 8;");
        }
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", 16));
        messageLabel.setTextFill(Color.WHITE);
        
        // Check if any player is defeated
        final boolean anyDefeated = isAnyPlayerDefeated();
        
        Button continueButton = new Button("Continue");
        continueButton.setOnAction(event -> {
            if (anyDefeated) {
                // Game over if any player is defeated
                showGameResult("Game Over! Some players have been defeated!");
            } else {
                presentNextObstacle();
            }
        });
        
        resultBox.getChildren().addAll(titleLabel, messageLabel, continueButton);
        centerPanel.getChildren().add(resultBox);
    }
    
    /**
     * Checks if any player is defeated.
     * 
     * @return true if any player is defeated, false otherwise
     */
    private boolean isAnyPlayerDefeated() {
        for (Player player : players) {
            if (player.isDefeated()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Shows the final game result.
     */
    private void showGameResult(String message) {
        centerPanel.getChildren().clear();
        
        VBox resultBox = new VBox(10);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(20));
        resultBox.setStyle("-fx-background-color: #2d4b6e; -fx-background-radius: 8;");
        
        Label titleLabel = new Label("Game Over");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);
        
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", 20));
        messageLabel.setTextFill(Color.WHITE);
        
        Button restartButton = new Button("Play Again");
        restartButton.setOnAction(event -> startGame());
        
        resultBox.getChildren().addAll(titleLabel, messageLabel, restartButton);
        centerPanel.getChildren().add(resultBox);
    }
}