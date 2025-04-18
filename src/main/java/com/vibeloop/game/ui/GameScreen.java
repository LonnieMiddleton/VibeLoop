package com.vibeloop.game.ui;

import com.vibeloop.game.model.Card;
import com.vibeloop.game.model.Player;
import com.vibeloop.game.model.Character;
import com.vibeloop.game.model.ObstacleCard;
import com.vibeloop.game.model.ObstacleDeck;
import com.vibeloop.game.service.ObstacleService;
import com.vibeloop.game.service.CardService;
import com.vibeloop.game.service.GameConfigService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Main game screen showing player profiles, decks, discard piles, and hands.
 */
public class GameScreen {
    private final Stage stage;
    private final List<Player> players;
    private final ObstacleService obstacleService;
    private final CardService cardService;
    private final GameConfigService configService;
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
    private Map<Player, Polygon> playerTurnArrows;
    
    // Preserve the original order of the obstacle deck for time loop mechanic
    private List<ObstacleCard> originalObstacleDeckOrder;
    
    // Track obstacle results in the current loop
    private List<ObstacleResult> obstacleHistory = new ArrayList<>();
    private ScrollPane historyScrollPane;
    private HBox historyBar;
    
    // New variables for tracking time loops and determining loss conditions
    private int currentLoop = 1; // Start with loop 1
    private int maxObstaclesPassed = 0; // Track max obstacles passed in previous loops
    
    // Inner class to track obstacle results
    private static class ObstacleResult {
        private final ObstacleCard obstacle;
        private final boolean succeeded;
        
        public ObstacleResult(ObstacleCard obstacle, boolean succeeded) {
            this.obstacle = obstacle;
            this.succeeded = succeeded;
        }
        
        public ObstacleCard getObstacle() {
            return obstacle;
        }
        
        public boolean isSucceeded() {
            return succeeded;
        }
    }
    
    // Add class field to store references to important UI components
    private FlowPane playedCardsPane;
    
    public GameScreen(Stage stage, List<Player> players, ObstacleService obstacleService) {
        this.stage = stage;
        this.players = players;
        this.obstacleService = obstacleService;
        this.cardService = new CardService();
        this.configService = new GameConfigService();
        this.obstacleDeck = obstacleService.createObstacleDeck();
        this.playedCards = new HashMap<>();
        this.playerHandPanes = new HashMap<>();
        this.playerStatusLabels = new HashMap<>();
        this.playerTurnArrows = new HashMap<>();
        this.originalObstacleDeckOrder = new ArrayList<>(obstacleDeck.getAllCards());
        this.obstacleHistory = new ArrayList<>();
        
        // Log deck configuration
        System.out.println("Created obstacle deck with " + this.obstacleDeck.getAllCards().size() + 
                          " cards, shuffle setting: " + configService.shouldShuffleObstacleDeck());
    }
    
    /**
     * Shows the game screen with player profiles, decks, and hands.
     */
    public void show() {
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
        stage.setFullScreen(true);
        stage.show();
        
        // Start the game
        startGame();
    }
    
    /**
     * Initializes players' hands by drawing 3 cards for each player.
     */
    private void initializePlayersHands() {
        for (Player player : players) {
            System.out.println("\n=== Initializing hand for " + player.getName() + " (" + player.getSelectedCharacter().getType() + ") ===");
            System.out.println("Before shuffle - Draw pile size: " + player.getDeck().getDrawPile().size());
            
            player.getDeck().shuffle();
            System.out.println("After shuffle - Draw pile size: " + player.getDeck().getDrawPile().size());
            
            System.out.println("Drawing initial hand...");
            int initialHandSize = player.getDeck().getHand().size();
            player.getDeck().drawCards(3);
            int finalHandSize = player.getDeck().getHand().size();
            
            System.out.println("Cards drawn: " + (finalHandSize - initialHandSize));
            System.out.println("Current hand size: " + player.getDeck().getHand().size());
            System.out.println("Remaining draw pile size: " + player.getDeck().getDrawPile().size());
            System.out.println("Cards in hand:");
            for (Card card : player.getDeck().getHand()) {
                System.out.println("- " + card.getName() + " (" + card.getStat() + ")");
            }
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
            // Create turn arrow for this player (initially invisible) - now pointing RIGHT
            Polygon turnArrow = new Polygon(
                0, 0,
                0, 40,
                30, 20  // Changed from -30 to 30 to point right instead of left
            );
            turnArrow.setFill(Color.YELLOW);
            turnArrow.setStroke(Color.ORANGE);
            turnArrow.setStrokeWidth(2);
            turnArrow.setVisible(false);
            playerTurnArrows.put(player, turnArrow);
            
            // Create arrow container
            StackPane arrowPane = new StackPane(turnArrow);
            arrowPane.setAlignment(Pos.CENTER_LEFT);
            arrowPane.setPrefWidth(40);
            
            // Create player section
            VBox playerSection = createPlayerSection(player);
            
            // Create horizontal layout with arrow and player section
            HBox playerRow = new HBox(5);
            playerRow.setAlignment(Pos.CENTER_LEFT);
            playerRow.getChildren().addAll(arrowPane, playerSection);
            
            playersPanel.getChildren().add(playerRow);
        }
        
        return playersPanel;
    }
    
    /**
     * Creates a section for a single player with profile, deck, discard pile, and hand.
     */
    private VBox createPlayerSection(Player player) {
        VBox playerSection = new VBox(3); // Reduced spacing from 5 to 3
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
        VBox nameContainer = new VBox(0); // Zero spacing in the name container
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
        playerSection.getChildren().addAll(nameContainer, mainRow, deckRow);
        
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
     * Starts a new game or time loop.
     */
    private void startGame() {
        System.out.println("\n=== Starting New Game ===");
        // Initialize the obstacle deck if we're starting a brand new game
        if (originalObstacleDeckOrder.isEmpty()) {
            System.out.println("First game - Creating and shuffling new obstacle deck");
            // This is the first game, create and store the original obstacle deck
            obstacleDeck = obstacleService.createObstacleDeck();
            // The shuffle is handled by ObstacleService based on config settings
            
            // Store the original order AFTER potential shuffling - this preserves the order for subsequent loops
            originalObstacleDeckOrder = new ArrayList<>(obstacleDeck.getAllCards());
            
            // Reset loop variables
            currentLoop = 1;
            maxObstaclesPassed = 0;
        } else {
            System.out.println("Subsequent game/loop - Resetting obstacle deck to original order");
            // For subsequent games (after time loops), reset to original order
            resetObstacleDeckToOriginalOrder();
            
            // Reset the game entirely if player chooses "Play Again" at the end
            if (currentLoop == 1) {
                System.out.println("New game via 'Play Again' - Creating new obstacle deck");
                // This is a new game (via "Play Again"), so create a new deck
                obstacleDeck = obstacleService.createObstacleDeck();
                // And store the new order
                originalObstacleDeckOrder = new ArrayList<>(obstacleDeck.getAllCards());
            }
            // Otherwise, for time loops within the same game, we use the same obstacle sequence
            
            // Clear obstacle history
            obstacleHistory.clear();
        }
        
        System.out.println("Obstacle deck size: " + obstacleDeck.getDrawPile().size());
        
        System.out.println("\n=== Resetting Players ===");
        // Reset all players' health to full
        for (Player player : players) {
            System.out.println("Resetting " + player.getName() + " (" + player.getSelectedCharacter().getType() + ")");
            System.out.println("Initial deck state:");
            System.out.println("- Draw pile: " + player.getDeck().getDrawPile().size());
            System.out.println("- Hand: " + player.getDeck().getHand().size());
            System.out.println("- Discard: " + player.getDeck().getDiscardPile().size());
            
            player.heal(player.getSelectedCharacter().getHealth());
            updatePlayerUI(player);
            
            System.out.println("After reset:");
            System.out.println("- Draw pile: " + player.getDeck().getDrawPile().size());
            System.out.println("- Hand: " + player.getDeck().getHand().size());
            System.out.println("- Discard: " + player.getDeck().getDiscardPile().size());
        }
        
        // Initialize players' hands
        System.out.println("\n=== Initializing Player Hands ===");
        initializePlayersHands();
        
        // Start with the first player
        currentPlayerIndex = 0;
        
        // Present the first obstacle
        presentNextObstacle();
    }
    
    /**
     * Resets obstacle deck to original order without shuffling for time loop mechanic.
     */
    private void resetObstacleDeckToOriginalOrder() {
        // Create a new obstacle deck with the original card order
        obstacleDeck = new ObstacleDeck();
        
        // Add cards in the original order
        for (ObstacleCard card : originalObstacleDeckOrder) {
            obstacleDeck.addCard(card);
        }
        
        // Reset active and defeated obstacles
        obstacleDeck.clearObstacles();
        
        // In time loops (currentLoop > 1), we want to maintain the same card order
        // But for new games (currentLoop == 1), check the config shuffle setting
        if (currentLoop == 1 && configService.shouldShuffleObstacleDeck()) {
            System.out.println("Shuffling obstacle deck based on config setting");
            obstacleDeck.shuffle();
        }
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
            // No more obstacles, loop is complete
            
            // In loop 1 or if we've encountered more obstacles than previous loops, it's a win
            int currentObstaclesEncountered = getTotalObstaclesEncountered();
            int successfulObstacles = getSuccessfulObstacleCount();
            
            if (currentLoop == 1 || currentObstaclesEncountered > maxObstaclesPassed) {
                // Victory! All obstacles overcome and we've made progress
                showGameResult("Victory! All obstacles have been overcome! You completed Loop " + 
                              currentLoop + " and encountered " + currentObstaclesEncountered + 
                              " obstacles (" + successfulObstacles + " successful).");
            } else {
                // Failed to make more progress than previous loop - loss condition 2
                showGameResult("Game Over! You failed to make more progress than your previous loop. " +
                              "You encountered " + currentObstaclesEncountered + " obstacles, but needed to encounter at least " + 
                              (maxObstaclesPassed + 1) + ".");
            }
            return;
        }
        
        // Hide all player arrows first
        for (Player player : players) {
            playerTurnArrows.get(player).setVisible(false);
        }
        
        // Set first player as active
        currentPlayerIndex = 0;
        // Show the arrow for the current player
        playerTurnArrows.get(players.get(currentPlayerIndex)).setVisible(true);
        
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
        
        // Add history bar to the top of center panel
        VBox historyPanel = createHistoryPanel();
        centerPanel.getChildren().add(historyPanel);
        
        // Add loop information
        HBox loopInfoBox = new HBox(10);
        loopInfoBox.setAlignment(Pos.CENTER);
        loopInfoBox.setPadding(new Insets(5));
        loopInfoBox.setStyle("-fx-background-color: #1a3245; -fx-background-radius: 8;");
        
        Label loopLabel = new Label("Time Loop: " + currentLoop);
        loopLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loopLabel.setTextFill(Color.LIGHTBLUE);
        
        // Show progress requirement if not the first loop
        if (currentLoop > 1) {
            Label progressLabel = new Label("Required Progress: " + (maxObstaclesPassed + 1) + 
                                           " obstacles (previous: " + maxObstaclesPassed + ")");
            progressLabel.setFont(Font.font("System", 14));
            progressLabel.setTextFill(Color.LIGHTYELLOW);
            
            Label currentProgressLabel = new Label("Current: " + getTotalObstaclesEncountered() + 
                                                " encountered (" + getSuccessfulObstacleCount() + " successful)");
            currentProgressLabel.setFont(Font.font("System", 14));
            currentProgressLabel.setTextFill(Color.LIGHTYELLOW);
            
            loopInfoBox.getChildren().addAll(loopLabel, progressLabel, currentProgressLabel);
        } else {
            Label currentProgressLabel = new Label("Obstacles: " + getTotalObstaclesEncountered() + 
                                                " encountered (" + getSuccessfulObstacleCount() + " successful)");
            currentProgressLabel.setFont(Font.font("System", 14));
            currentProgressLabel.setTextFill(Color.LIGHTYELLOW);
            
            loopInfoBox.getChildren().addAll(loopLabel, currentProgressLabel);
        }
        
        centerPanel.getChildren().add(loopInfoBox);
        
        // Update history bar
        updateHistoryBar();
        
        // Check if this is the finale obstacle
        boolean isFinale = currentObstacle.isFinale();
        
        // Obstacle title
        Label titleLabel = new Label(isFinale ? "FINAL OBSTACLE" : "Current Obstacle");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, isFinale ? 24 : 20));
        titleLabel.setTextFill(isFinale ? Color.RED : Color.WHITE);
        
        // Obstacle card display
        VBox obstacleBox = new VBox(10);
        obstacleBox.setAlignment(Pos.CENTER);
        obstacleBox.setPadding(new Insets(10));
        
        // Use special styling for finale
        if (isFinale) {
            obstacleBox.setStyle("-fx-background-color: #4a0000; -fx-background-radius: 8; -fx-border-color: #ff0000; -fx-border-width: 3; -fx-border-radius: 8;");
        } else {
            obstacleBox.setStyle("-fx-background-color: #2d4b6e; -fx-background-radius: 8;");
        }
        
        // Obstacle image
        ImageView obstacleImage;
        try {
            Image image = new Image(getClass().getResourceAsStream(currentObstacle.getImagePath()));
            obstacleImage = new ImageView(image);
            
            // Make finale image larger
            if (isFinale) {
                obstacleImage.setFitWidth(CARD_WIDTH * 3);
                obstacleImage.setFitHeight(CARD_HEIGHT * 3);
                
                // Add a glow effect to the finale image
                DropShadow glow = new DropShadow();
                glow.setColor(Color.RED);
                glow.setWidth(20);
                glow.setHeight(20);
                glow.setRadius(10);
                obstacleImage.setEffect(glow);
            } else {
                obstacleImage.setFitWidth(CARD_WIDTH * 2);
                obstacleImage.setFitHeight(CARD_HEIGHT * 2);
            }
            
            obstacleBox.getChildren().add(obstacleImage);
        } catch (Exception e) {
            // If image can't be loaded, use a placeholder
            Rectangle placeholder = new Rectangle(
                isFinale ? CARD_WIDTH * 3 : CARD_WIDTH * 2, 
                isFinale ? CARD_HEIGHT * 3 : CARD_HEIGHT * 2
            );
            placeholder.setFill(isFinale ? Color.RED.deriveColor(0, 1, 1, 0.3) : Color.GRAY.deriveColor(0, 1, 1, 0.3));
            placeholder.setStroke(isFinale ? Color.RED : Color.WHITE);
            obstacleBox.getChildren().add(placeholder);
            System.err.println("Error loading obstacle image: " + e.getMessage());
        }
        
        // Obstacle details
        Label nameLabel = new Label(currentObstacle.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, isFinale ? 20 : 16));
        nameLabel.setTextFill(isFinale ? Color.RED : Color.WHITE);
        
        Label difficultyLabel = new Label("Difficulty: " + currentObstacle.getDifficulty());
        difficultyLabel.setFont(Font.font("System", isFinale ? 16 : 14));
        difficultyLabel.setTextFill(isFinale ? Color.ORANGE : Color.WHITE);
        
        // Add obstacle type with a color based on the type
        Label typeLabel = new Label("Type: " + currentObstacle.getType());
        typeLabel.setFont(Font.font("System", isFinale ? 16 : 14));
        
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
            case "finale":
                typeLabel.setTextFill(Color.RED);
                break;
            default:
                typeLabel.setTextFill(Color.WHITE);
        }
        
        // Add required skills
        String[] requiredSkills = currentObstacle.getRequiredSkills();
        
        if (isFinale) {
            // For finale, show the specific requirements for each obstacle type
            Label skillsLabel = new Label("This challenge requires different obstacle types to be resolved:");
            skillsLabel.setFont(Font.font("System", 16));
            skillsLabel.setTextFill(Color.LIGHTYELLOW);
            
            // Create a special breakdown of required obstacle types
            VBox requirementsBox = new VBox(5);
            requirementsBox.setAlignment(Pos.CENTER_LEFT);
            requirementsBox.setPadding(new Insets(10));
            requirementsBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 5;");
            
            // Calculate current progress on each obstacle type
            int currentEnvironment = 0;
            int currentHazard = 0;
            int currentBarrier = 0;
            
            for (Map.Entry<Player, Card> entry : playedCards.entrySet()) {
                Player player = entry.getKey();
                Card card = entry.getValue();
                
                if (card != null) {
                    // Get the card's contribution based on player's stats
                    int contribution = currentObstacle.getCardContribution(card, player);
                    
                    // Check each of the card's compatible types
                    for (String cardType : card.getCompatibleTypes()) {
                        if ("environment".equalsIgnoreCase(cardType)) {
                            currentEnvironment += contribution;
                        } else if ("hazard".equalsIgnoreCase(cardType)) {
                            currentHazard += contribution;
                        } else if ("barrier".equalsIgnoreCase(cardType)) {
                            currentBarrier += contribution;
                        }
                    }
                }
            }
            
            int environmentRequired = currentObstacle.getEnvironmentRequired();
            int hazardRequired = currentObstacle.getHazardRequired();
            int barrierRequired = currentObstacle.getBarrierRequired();
            
            // Create progress bars for each type
            Label environmentLabel = new Label("Environment: " + currentEnvironment + "/" + environmentRequired);
            environmentLabel.setTextFill(Color.LIGHTBLUE);
            environmentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            ProgressBar environmentBar = new ProgressBar(Math.min(1.0, (double)currentEnvironment / environmentRequired));
            environmentBar.setPrefWidth(300);
            environmentBar.setStyle("-fx-accent: #1e90ff;"); // Dodger blue
            
            Label hazardLabel = new Label("Hazard: " + currentHazard + "/" + hazardRequired);
            hazardLabel.setTextFill(Color.LIGHTSALMON);
            hazardLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            ProgressBar hazardBar = new ProgressBar(Math.min(1.0, (double)currentHazard / hazardRequired));
            hazardBar.setPrefWidth(300);
            hazardBar.setStyle("-fx-accent: #ff6347;"); // Tomato color
            
            Label barrierLabel = new Label("Barrier: " + currentBarrier + "/" + barrierRequired);
            barrierLabel.setTextFill(Color.LIGHTGREEN);
            barrierLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            ProgressBar barrierBar = new ProgressBar(Math.min(1.0, (double)currentBarrier / barrierRequired));
            barrierBar.setPrefWidth(300);
            barrierBar.setStyle("-fx-accent: #32cd32;"); // Lime green
            
            requirementsBox.getChildren().addAll(
                new HBox(10, environmentLabel, environmentBar),
                new HBox(10, hazardLabel, hazardBar),
                new HBox(10, barrierLabel, barrierBar)
            );
            
            // Add a note about using any skill type
            Label compatibilityNote = new Label("Any skill (strength, speed, tech) can be used for any obstacle type!");
            compatibilityNote.setTextFill(Color.YELLOW);
            compatibilityNote.setFont(Font.font("System", FontWeight.BOLD, 14));
            requirementsBox.getChildren().add(compatibilityNote);
            
            // Add description
            Label descriptionLabel = new Label(currentObstacle.getDescription());
            descriptionLabel.setFont(Font.font("System", 16));
            descriptionLabel.setTextFill(Color.WHITE);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(500);
            
            obstacleBox.getChildren().addAll(nameLabel, difficultyLabel, typeLabel, skillsLabel, requirementsBox, descriptionLabel);
        } else {
            // Regular skill display for normal obstacles
            StringBuilder skillsText = new StringBuilder("Required Skills: ");
            for (int i = 0; i < requiredSkills.length; i++) {
                skillsText.append(requiredSkills[i]);
                if (i < requiredSkills.length - 1) {
                    skillsText.append(", ");
                }
            }
            
            Label skillsLabel = new Label(skillsText.toString());
            skillsLabel.setFont(Font.font("System", 14));
            skillsLabel.setTextFill(Color.WHITE);
            
            // Add description
            Label descriptionLabel = new Label(currentObstacle.getDescription());
            descriptionLabel.setFont(Font.font("System", 14));
            descriptionLabel.setTextFill(Color.WHITE);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(500);
            
            // Add progress bar
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(300);
            progressBar.setStyle("-fx-accent: #4287f5;");
            
            // Calculate progress
            int totalProgress = 0;
            
            // Calculate current progress from played cards
            for (Map.Entry<Player, Card> entry : playedCards.entrySet()) {
                Player player = entry.getKey();
                Card card = entry.getValue();
                
                if (card != null && card.isCompatibleWithType(currentObstacle.getType())) {
                    String cardStat = card.getStat().toLowerCase();
                    for (String requiredSkill : currentObstacle.getRequiredSkills()) {
                        if (cardStat.equals(requiredSkill.toLowerCase())) {
                            switch (cardStat) {
                                case "strength":
                                    totalProgress += player.getSelectedCharacter().getStrength();
                                    break;
                                case "speed":
                                    totalProgress += player.getSelectedCharacter().getSpeed();
                                    break;
                                case "tech":
                                    totalProgress += player.getSelectedCharacter().getTech();
                                    break;
                            }
                        }
                    }
                }
            }
            
            // Set progress bar value (clamped between 0 and 1)
            double progress = Math.min(1.0, (double) totalProgress / currentObstacle.getDifficulty());
            progressBar.setProgress(progress);
            
            // Add progress label
            Label progressLabel = new Label("Progress: " + totalProgress + " / " + currentObstacle.getDifficulty());
            progressLabel.setFont(Font.font("System", 14));
            progressLabel.setTextFill(Color.WHITE);
            
            obstacleBox.getChildren().addAll(nameLabel, difficultyLabel, typeLabel, skillsLabel, descriptionLabel, progressBar, progressLabel);
        }
        
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
        
        // Create placeholders for all players' cards upfront to prevent layout shifts
        for (Player player : players) {
            VBox cardBox = new VBox(2);
            cardBox.setAlignment(Pos.CENTER);
            
            // Check if this player has already played a card
            Card card = playedCards.get(player);
            
            if (card != null) {
                // Player has already played a card, show it
                addCardToPlayedCardsPane(player, card, cardBox);
            } else {
                // No card played yet, show placeholder
                Rectangle placeholderRect = new Rectangle(CARD_WIDTH * 1.5, CARD_HEIGHT * 1.5);
                placeholderRect.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.2));
                placeholderRect.setStroke(Color.WHITE);
                placeholderRect.setStrokeWidth(0.5);
                
                Text waitingText = new Text("Waiting...");
                waitingText.setFill(Color.WHITE);
                waitingText.setFont(Font.font("System", FontWeight.NORMAL, 12));
                
                StackPane placeholderPane = new StackPane();
                placeholderPane.getChildren().addAll(placeholderRect, waitingText);
                
                cardBox.getChildren().add(placeholderPane);
            }
            
            // Player name
            Label playerLabel = new Label(player.getName());
            playerLabel.setTextFill(Color.WHITE);
            cardBox.getChildren().add(playerLabel);
            
            // Add current player indicator if this is the active player
            if (players.indexOf(player) == currentPlayerIndex) {
                Rectangle indicator = new Rectangle(10, 10);
                indicator.setFill(Color.YELLOW);
                indicator.setTranslateX(-5);
                
                StackPane nameWithIndicator = new StackPane();
                nameWithIndicator.setAlignment(Pos.CENTER_LEFT);
                nameWithIndicator.getChildren().addAll(indicator, playerLabel);
                
                // Replace the regular name label with the one with indicator
                cardBox.getChildren().remove(playerLabel);
                cardBox.getChildren().add(nameWithIndicator);
            }
            
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
     * Helper method to add a card to the played cards pane
     */
    private void addCardToPlayedCardsPane(Player player, Card card, VBox cardBox) {
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
    }
    
    /**
     * Updates the played card display when a card is played or skipped, without recreating the entire layout.
     */
    private void updatePlayedCardDisplay(Player player, Card card) {
        try {
            // Log for debugging
            System.out.println("Updating played card display for " + player.getName());
            
            if (playedCardsPane == null) {
                System.err.println("Played cards pane not initialized, recreating obstacle display");
                updateObstacleDisplay();
                return;
            }
            
            int playerIndex = players.indexOf(player);
            if (playerIndex < 0 || playerIndex >= playedCardsPane.getChildren().size()) {
                System.err.println("Player index out of bounds: " + playerIndex);
                return;
            }
            
            // Find the player's card container by index
            Node cardContainerNode = playedCardsPane.getChildren().get(playerIndex);
            System.out.println("Found card container of type: " + cardContainerNode.getClass().getName());
            
            if (!(cardContainerNode instanceof VBox)) {
                System.err.println("Expected VBox for player card container, found: " + cardContainerNode.getClass().getName());
                // Recreate the entire obstacle display as a fallback
                updateObstacleDisplay();
                return;
            }
            
            VBox cardBox = (VBox) cardContainerNode;
            
            // Find and remove the card or placeholder (first child)
            if (cardBox.getChildren().isEmpty()) {
                System.err.println("Card box is empty");
                return;
            }
            
            // Remove the first element (card or placeholder)
            cardBox.getChildren().remove(0);
            
            if (card != null) {
                // Add the played card
                addCardToPlayedCardsPane(player, card, cardBox);
            } else {
                // Add a "SKIPPED" indicator
                Rectangle skipRect = new Rectangle(CARD_WIDTH * 1.5, CARD_HEIGHT * 1.5);
                skipRect.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.2));
                skipRect.setStroke(Color.WHITE);
                
                Text skipText = new Text("SKIPPED");
                skipText.setFill(Color.WHITE);
                skipText.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                // Stack the text on top of the rectangle
                javafx.scene.layout.StackPane skipPane = new javafx.scene.layout.StackPane();
                skipPane.getChildren().addAll(skipRect, skipText);
                
                cardBox.getChildren().add(0, skipPane);
            }
            
            System.out.println("Successfully updated played card for " + player.getName());
        } catch (Exception e) {
            System.err.println("Error updating played card display: " + e.getMessage());
            e.printStackTrace();
            // Fallback - recreate the whole obstacle display
            updateObstacleDisplay();
        }
    }
    
    /**
     * Handles a player playing a card.
     */
    private void playCard(Player player, Card card) {
        // Check if player is allowed to play cards (must be their turn)
        if (players.indexOf(player) != currentPlayerIndex) {
            System.out.println("Not " + player.getName() + "'s turn!");
            return;
        }
        
        // Check if card is compatible with current obstacle type
        boolean isCompatible = currentObstacle.isFinale() || card.isCompatibleWithType(currentObstacle.getType());
        
        // Even if not compatible, still allow playing, but warn the player
        if (!isCompatible) {
            System.out.println("Warning: " + card.getName() + " is not compatible with " + 
                               currentObstacle.getType() + " obstacles!");
        }
        
        // Move card from hand to played cards
        player.getDeck().playCard(card);
        playedCards.put(player, card);
        
        // Update the display
        updatePlayedCardDisplay(player, card);
        
        // Move to next player
        advanceToNextPlayer();
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
        // Hide the current player's arrow
        playerTurnArrows.get(players.get(currentPlayerIndex)).setVisible(false);
        
        // Move to next player
        currentPlayerIndex++;
        
        // Check if all players have played
        if (currentPlayerIndex >= players.size()) {
            // All players have played, resolve the obstacle
            resolveObstacle();
        } else {
            // Show the next player's arrow
            playerTurnArrows.get(players.get(currentPlayerIndex)).setVisible(true);
            
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
        
        // Special handling for finale obstacle
        if (currentObstacle.isFinale()) {
            int totalEnvironment = 0;
            int totalHazard = 0;
            int totalBarrier = 0;
            
            // For each player who played a card, collect stats for each category
            for (Map.Entry<Player, Card> entry : playedCards.entrySet()) {
                Player player = entry.getKey();
                Card card = entry.getValue();
                Character character = player.getSelectedCharacter();
                
                if (card != null) {
                    // For finale, check each of the card's compatible types
                    // Get the value based on the player's skill
                    int contribution = currentObstacle.getCardContribution(card, player);
                    boolean contributed = false;
                    
                    // Check which obstacle types this card can address
                    for (String cardType : card.getCompatibleTypes()) {
                        if ("environment".equalsIgnoreCase(cardType)) {
                            totalEnvironment += contribution;
                            contributed = true;
                        } else if ("hazard".equalsIgnoreCase(cardType)) {
                            totalHazard += contribution;
                            contributed = true;
                        } else if ("barrier".equalsIgnoreCase(cardType)) {
                            totalBarrier += contribution;
                            contributed = true;
                        }
                    }
                    
                    if (contributed) {
                        skillBreakdown.append(player.getName()).append(": ").append(card.getName())
                            .append(" (").append(card.getStat()).append(" ").append(contribution).append(") - ");
                        
                        StringBuilder typesList = new StringBuilder();
                        for (String cardType : card.getCompatibleTypes()) {
                            if ("environment".equalsIgnoreCase(cardType) || 
                                "hazard".equalsIgnoreCase(cardType) || 
                                "barrier".equalsIgnoreCase(cardType)) {
                                if (typesList.length() > 0) {
                                    typesList.append(", ");
                                }
                                typesList.append(cardType);
                            }
                        }
                        
                        skillBreakdown.append("Types: ").append(typesList).append("\n");
                    } else {
                        skillBreakdown.append(player.getName()).append(": ").append(card.getName())
                            .append(" (").append(card.getStat()).append(" ").append(contribution)
                            .append(") - No compatible obstacle types\n");
                    }
                } else {
                    // Player skipped
                    skillBreakdown.append(player.getName()).append(": Skipped (0)\n");
                }
            }
            
            // Calculate if each required obstacle type threshold was met
            boolean environmentMet = totalEnvironment >= currentObstacle.getEnvironmentRequired();
            boolean hazardMet = totalHazard >= currentObstacle.getHazardRequired();
            boolean barrierMet = totalBarrier >= currentObstacle.getBarrierRequired();
            
            // All thresholds must be met to succeed
            boolean succeeded = environmentMet && hazardMet && barrierMet;
            
            // Show skill breakdown for finale
            skillBreakdown.append("\nEnvironment: ").append(totalEnvironment).append("/").append(currentObstacle.getEnvironmentRequired())
                .append(environmentMet ? " ✓" : " ✗").append("\n");
            skillBreakdown.append("Hazard: ").append(totalHazard).append("/").append(currentObstacle.getHazardRequired())
                .append(hazardMet ? " ✓" : " ✗").append("\n");
            skillBreakdown.append("Barrier: ").append(totalBarrier).append("/").append(currentObstacle.getBarrierRequired())
                .append(barrierMet ? " ✓" : " ✗").append("\n");
            
            // Add obstacle to history
            obstacleHistory.add(new ObstacleResult(currentObstacle, succeeded));
            updateHistoryBar();
            
            if (succeeded) {
                // Success! Players overcome the finale
                obstacleDeck.defeatObstacle(currentObstacle);
                
                // Add a random card to each player as a reward for the finale
                for (Player player : players) {
                    addRandomCardToPlayer(player);
                }
                
                showObstacleResult("SUCCESS! NUCLEAR CORE STABILIZED", 
                                 "The team has successfully stabilized the nuclear core and saved the station!\n\n" +
                                 skillBreakdown.toString(),
                                 true);
            } else {
                // Failure - players take damage based on how many requirements weren't met
                int unmetRequirements = 0;
                if (!environmentMet) unmetRequirements++;
                if (!hazardMet) unmetRequirements++;
                if (!barrierMet) unmetRequirements++;
                
                int damage = unmetRequirements * 2; // 2 damage per unmet requirement
                distributeAndApplyDamage(damage);
                
                showObstacleResult("FAILURE! NUCLEAR CORE CRITICAL", 
                                 "The team failed to stabilize the nuclear core!\n" +
                                 "Players Take " + damage + " Damage for unmet requirements.\n\n" +
                                 skillBreakdown.toString(),
                                 false);
            }
        } else {
            // Regular obstacle resolution
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
            boolean succeeded = totalSkill >= obstacleDifficulty;
            
            // Add obstacle to history
            obstacleHistory.add(new ObstacleResult(currentObstacle, succeeded));
            updateHistoryBar();
            
            if (succeeded) {
                // Success! Players overcome the obstacle
                obstacleDeck.defeatObstacle(currentObstacle);
                
                // Add a random card to a random player's deck as reward for success
                addRandomCardToRandomPlayer();
                
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
    }
    
    /**
     * Adds a random card to a specific player's deck.
     * Called as a reward for the finale.
     */
    private void addRandomCardToPlayer(Player player) {
        // Get all available cards
        Map<String, Card> allCards = cardService.getAllCards();
        if (allCards.isEmpty()) {
            return;
        }
        
        // Convert to list to be able to get a random card
        List<Card> cardList = new ArrayList<>(allCards.values());
        int randomCardIndex = (int) (Math.random() * cardList.size());
        Card randomCard = cardList.get(randomCardIndex);
        
        // Add a copy of the card to the player's deck
        Card newCard = new Card(
            randomCard.getId(),
            randomCard.getName(),
            randomCard.getDescription(),
            randomCard.getStat(),
            randomCard.getCompatibleTypes()
        );
        
        // Add to player's deck (cards collection) and discard pile rather than draw pile
        player.getDeck().addCardToDiscard(newCard);
        
        // Display message about the new card
        System.out.println("New card awarded to " + player.getName() + ": " + newCard.getName() + " (added to discard pile)");
        
        // Update UI to reflect the new card in the deck
        updatePlayerUI(player);
    }
    
    /**
     * Adds a random card to a randomly selected player's deck.
     * Called as a reward when players successfully overcome an obstacle.
     */
    private void addRandomCardToRandomPlayer() {
        if (players.isEmpty()) {
            return;
        }
        
        // Select a random player
        int randomPlayerIndex = (int) (Math.random() * players.size());
        Player selectedPlayer = players.get(randomPlayerIndex);
        
        // Get all available cards
        Map<String, Card> allCards = cardService.getAllCards();
        if (allCards.isEmpty()) {
            return;
        }
        
        // Convert to list to be able to get a random card
        List<Card> cardList = new ArrayList<>(allCards.values());
        int randomCardIndex = (int) (Math.random() * cardList.size());
        Card randomCard = cardList.get(randomCardIndex);
        
        // Add a copy of the card to the player's deck
        Card newCard = new Card(
            randomCard.getId(),
            randomCard.getName(),
            randomCard.getDescription(),
            randomCard.getStat(),
            randomCard.getCompatibleTypes()
        );
        
        // Add to player's deck (cards collection) and discard pile rather than draw pile
        selectedPlayer.getDeck().addCardToDiscard(newCard);
        
        // Display message about the new card
        System.out.println("New card awarded to " + selectedPlayer.getName() + ": " + newCard.getName() + " (added to discard pile)");
        
        // Update UI to reflect the new card in the deck
        updatePlayerUI(selectedPlayer);
    }
    
    /**
     * Distributes and applies damage to players.
     */
    private void distributeAndApplyDamage(int totalDamage) {
        // Simple distribution - divide damage equally among players
        int playersCount = players.size();
        int damagePerPlayer = totalDamage / playersCount;
        int remainingDamage = totalDamage % playersCount;
        
        boolean anyPlayerDefeated = false;
        
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
            
            if (player.isDefeated()) {
                anyPlayerDefeated = true;
                System.out.println(player.getName() + " has been defeated! Time loop activated!");
            }
        }
        
        // Changed to check if ANY player is defeated for time loop mechanic
        if (anyPlayerDefeated) {
            // One or more players are defeated, but we'll handle this in showObstacleResult
            System.out.println("At least one player has been defeated, time loop will be activated");
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
        messageLabel.setFont(Font.font("System", 20));
        messageLabel.setTextFill(Color.WHITE);
        
        // Check if any player is defeated
        final boolean anyDefeated = isAnyPlayerDefeated();
        
        // Check if no more obstacles and we're not on the first loop - need to verify if we've surpassed previous loop
        final boolean isLoopComplete = obstacleDeck.isEmpty();
        if (isLoopComplete && currentLoop > 1) {
            int currentObstaclesEncountered = getTotalObstaclesEncountered();
            
            // Loss condition 2: Failed to exceed previous loop's progress
            if (currentObstaclesEncountered <= maxObstaclesPassed) {
                showGameResult("Game Over! You failed to make more progress than your previous loop. " +
                              "You encountered " + currentObstaclesEncountered + " obstacles, but needed to encounter at least " + 
                              (maxObstaclesPassed + 1) + ".");
                return;
            }
        }
        
        Button continueButton = new Button("Continue");
        
        // Add notification about awarded card if success
        if (success) {
            Label rewardLabel = new Label("A random card has been added to one player's deck!");
            rewardLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            rewardLabel.setTextFill(Color.YELLOW);
            resultBox.getChildren().add(rewardLabel);
        }
        
        if (anyDefeated) {
            // Update button text for time loop
            continueButton.setText("Enter Time Loop");
            continueButton.setStyle("-fx-background-color: #8a2be2;"); // Purple background for time loop
            
            // Add explanation about time loop
            Label timeLoopLabel = new Label("A player has reached 0 health! The time loop has been activated.");
            timeLoopLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            timeLoopLabel.setTextFill(Color.LIGHTYELLOW);
            resultBox.getChildren().add(timeLoopLabel);
        }
        
        continueButton.setOnAction(event -> {
            if (anyDefeated) {
                // Start time loop mechanic when a player is defeated
                beginTimeLoop();
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
     * Begins the time loop mechanic when a player is defeated.
     */
    private void beginTimeLoop() {
        // Update the maximum number of obstacles passed in previous loops
        int obstaclesEncountered = getTotalObstaclesEncountered();
        
        // Check if we're beyond loop 1 and failed to make more progress
        if (currentLoop > 1 && obstaclesEncountered <= maxObstaclesPassed) {
            // Failed to make more progress than previous loop - loss condition 2
            showGameResult("Game Over! You failed to make more progress than your previous loop. " +
                          "You encountered " + obstaclesEncountered + " obstacles, but needed to encounter at least " + 
                          (maxObstaclesPassed + 1) + ".");
            return;
        }
        
        maxObstaclesPassed = Math.max(maxObstaclesPassed, obstaclesEncountered);
        
        // Increment the loop counter
        currentLoop++;
        
        // If this is the second loop (after first loop), check if all objectives failed in first loop
        if (currentLoop == 2 && getSuccessfulObstacleCount() == 0) {
            // Players failed all objectives on first loop - loss condition 1
            showGameResult("Game Over! You failed all objectives on the first loop.");
            return;
        }
        
        // Reset the obstacle deck to its original order without shuffling
        resetObstacleDeckToOriginalOrder();
        
        // Clear obstacle history for the new loop
        obstacleHistory.clear();
        
        // Show the card removal screen for each player
        showCardRemovalScreen();
    }
    
    /**
     * Counts the number of successfully passed obstacles in the current loop.
     * @return The number of obstacles successfully passed
     */
    private int getSuccessfulObstacleCount() {
        int successCount = 0;
        for (ObstacleResult result : obstacleHistory) {
            if (result.isSucceeded()) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * Counts the total number of obstacles encountered in the current loop,
     * regardless of success or failure.
     * @return The total number of obstacles encountered
     */
    private int getTotalObstaclesEncountered() {
        return obstacleHistory.size();
    }
    
    /**
     * Shows a screen allowing each player to select a card to remove from their deck.
     */
    private void showCardRemovalScreen() {
        centerPanel.getChildren().clear();
        
        // Create a scrollable panel for all players' card selections
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox allPlayersBox = new VBox(20);
        allPlayersBox.setPadding(new Insets(10));
        allPlayersBox.setAlignment(Pos.TOP_CENTER);
        
        Label titleLabel = new Label("Time Loop Activated");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);
        
        Label instructionsLabel = new Label("Each player must select ONE card to remove from their deck before continuing");
        instructionsLabel.setFont(Font.font("System", 18));
        instructionsLabel.setTextFill(Color.LIGHTBLUE);
        
        allPlayersBox.getChildren().addAll(titleLabel, instructionsLabel);
        
        // Map to track selected cards for each player
        Map<Player, Card> selectedCardsToRemove = new HashMap<>();
        
        // Continue button (enabled only when all players have selected a card)
        Button continueButton = new Button("Continue to Next Loop");
        continueButton.setDisable(true);
        
        // Update the continue button state whenever a card is selected
        Runnable updateContinueButtonState = () -> {
            continueButton.setDisable(selectedCardsToRemove.size() < players.size());
        };
        
        // Create card display for each player
        for (Player player : players) {
            VBox playerBox = new VBox(10);
            playerBox.setPadding(new Insets(10));
            playerBox.setStyle("-fx-background-color: #2d4b6e; -fx-background-radius: 8;");
            
            Label playerLabel = new Label(player.getName() + "'s Cards");
            playerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            playerLabel.setTextFill(Color.WHITE);
            
            // Label to show selected card
            Label selectedCardLabel = new Label("No card selected");
            selectedCardLabel.setFont(Font.font("System", 14));
            selectedCardLabel.setTextFill(Color.ORANGE);
            
            playerBox.getChildren().addAll(playerLabel, selectedCardLabel);
            
            // Create a flow pane for all of the player's cards
            FlowPane cardsPane = new FlowPane(5, 5);
            cardsPane.setPadding(new Insets(5));
            
            // Get all of the player's cards (deck and hand combined)
            List<Card> allPlayerCards = new ArrayList<>();
            allPlayerCards.addAll(player.getDeck().getDrawPile());
            allPlayerCards.addAll(player.getDeck().getHand());
            allPlayerCards.addAll(player.getDeck().getDiscardPile());
            
            // Display all cards
            for (Card card : allPlayerCards) {
                StackPane cardPane = new StackPane();
                cardPane.setAlignment(Pos.BOTTOM_RIGHT);
                
                // Create the card visual
                boolean imageLoaded = false;
                
                // Try all possible paths for the card image
                String[] possiblePaths = {
                    "/cards/" + card.getId() + ".jpg",
                    "/cards/skills/" + card.getId() + ".jpg", 
                    "/cards/tools/" + card.getId() + ".jpg"
                };
                
                for (String path : possiblePaths) {
                    try {
                        InputStream is = getClass().getResourceAsStream(path);
                        if (is != null) {
                            Image cardImage = new Image(is);
                            ImageView cardView = new ImageView(cardImage);
                            cardView.setFitWidth(CARD_WIDTH * 1.5);
                            cardView.setFitHeight(CARD_HEIGHT * 1.5);
                            
                            Rectangle border = new Rectangle(
                                CARD_WIDTH * 1.5, 
                                CARD_HEIGHT * 1.5,
                                Color.TRANSPARENT
                            );
                            border.setStroke(Color.GRAY);
                            border.setStrokeWidth(1);
                            
                            cardPane.getChildren().addAll(cardView, border);
                            
                            // Add tooltip for card details
                            Tooltip cardTooltip = createCardTooltip(card);
                            Tooltip.install(cardPane, cardTooltip);
                            
                            imageLoaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading card image for " + card.getId() + ": " + e.getMessage());
                    }
                }
                
                // If no image was loaded, create a placeholder
                if (!imageLoaded) {
                    Rectangle placeholder = new Rectangle(CARD_WIDTH * 1.5, CARD_HEIGHT * 1.5, Color.DARKGRAY);
                    Text cardNameText = new Text(card.getName());
                    cardNameText.setFill(Color.WHITE);
                    cardNameText.setWrappingWidth(CARD_WIDTH * 1.3);
                    cardNameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                    
                    cardPane.getChildren().addAll(placeholder, cardNameText);
                }
                
                // Make cards selectable
                cardPane.setOnMouseClicked(event -> {
                    // Clear previous selection styling
                    for (int i = 0; i < cardsPane.getChildren().size(); i++) {
                        Node node = cardsPane.getChildren().get(i);
                        if (node instanceof StackPane) {
                            StackPane pane = (StackPane) node;
                            // Reset border
                            for (int j = 0; j < pane.getChildren().size(); j++) {
                                if (pane.getChildren().get(j) instanceof Rectangle) {
                                    ((Rectangle) pane.getChildren().get(j)).setStroke(Color.GRAY);
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Highlight selected card
                    for (Node node : cardPane.getChildren()) {
                        if (node instanceof Rectangle) {
                            ((Rectangle) node).setStroke(Color.YELLOW);
                            ((Rectangle) node).setStrokeWidth(3);
                            break;
                        }
                    }
                    
                    // Update selected card
                    selectedCardsToRemove.put(player, card);
                    selectedCardLabel.setText("Selected: " + card.getName());
                    
                    // Update continue button state
                    updateContinueButtonState.run();
                });
                
                cardsPane.getChildren().add(cardPane);
            }
            
            playerBox.getChildren().add(cardsPane);
            allPlayersBox.getChildren().add(playerBox);
        }
        
        // Continue button action
        continueButton.setOnAction(event -> {
            // Remove the selected cards from each player's deck
            for (Map.Entry<Player, Card> entry : selectedCardsToRemove.entrySet()) {
                Player player = entry.getKey();
                Card cardToRemove = entry.getValue();
                
                // Remove the card from player's deck
                player.getDeck().removeCard(cardToRemove);
                System.out.println("Removed card " + cardToRemove.getName() + " from " + player.getName() + "'s deck");
            }
            
            // For each player: reset health, move all cards to draw pile, and shuffle
            for (Player player : players) {
                // Print initial state
                System.out.println("BEFORE RESET - " + player.getName() + " has: " + 
                                  player.getDeck().getDrawPile().size() + " in draw pile, " + 
                                  player.getDeck().getHand().size() + " in hand, " + 
                                  player.getDeck().getDiscardPile().size() + " in discard, " + 
                                  player.getDeck().getCards().size() + " total cards");
                
                // Reset player health to full
                player.heal(player.getSelectedCharacter().getHealth());
                
                // Reset the player's deck for the time loop - moves all cards from hand and discard to draw pile and shuffles
                player.getDeck().resetDeckForTimeLoop();
                
                // Print state after reset
                System.out.println("AFTER RESET - " + player.getName() + " has: " + 
                                  player.getDeck().getDrawPile().size() + " in draw pile, " + 
                                  player.getDeck().getHand().size() + " in hand, " + 
                                  player.getDeck().getDiscardPile().size() + " in discard");
                
                // Draw initial hand of 3 cards (or as many as possible)
                player.getDeck().drawCards(3);
                
                // Print final state
                System.out.println("FINAL STATE - " + player.getName() + " now has " + 
                                  player.getDeck().getDrawPile().size() + " cards in draw pile, " + 
                                  player.getDeck().getHand().size() + " cards in hand, and " + 
                                  player.getDeck().getDiscardPile().size() + " cards in discard pile");
                
                // Update the UI to show the current state of the player's deck
                updatePlayerUI(player);
            }
            
            // Start a new game loop
            presentNextObstacle();
        });
        
        // Initial update of the continue button
        updateContinueButtonState.run();
        
        allPlayersBox.getChildren().add(continueButton);
        scrollPane.setContent(allPlayersBox);
        centerPanel.getChildren().add(scrollPane);
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
        restartButton.setOnAction(event -> {
            // Reset loop variables for a new game
            currentLoop = 1;
            maxObstaclesPassed = 0;
            startGame();
        });
        
        resultBox.getChildren().addAll(titleLabel, messageLabel, restartButton);
        centerPanel.getChildren().add(resultBox);
    }
    
    /**
     * Updates the history bar with completed obstacles
     */
    private void updateHistoryBar() {
        historyBar.getChildren().clear();
        
        if (obstacleHistory.isEmpty()) {
            Label emptyLabel = new Label("No obstacles completed yet in this loop");
            emptyLabel.setTextFill(Color.LIGHTGRAY);
            historyBar.getChildren().add(emptyLabel);
            return;
        }
        
        for (ObstacleResult result : obstacleHistory) {
            ObstacleCard obstacle = result.getObstacle();
            boolean succeeded = result.isSucceeded();
            
            // Create a mini view of the obstacle
            StackPane obstaclePane = new StackPane();
            
            // Obstacle image or placeholder
            ImageView miniImage = null;
            try {
                Image image = new Image(getClass().getResourceAsStream(obstacle.getImagePath()));
                miniImage = new ImageView(image);
                miniImage.setFitWidth(CARD_WIDTH * 0.7);
                miniImage.setFitHeight(CARD_HEIGHT * 0.7);
                obstaclePane.getChildren().add(miniImage);
            } catch (Exception e) {
                // If image can't be loaded, use a placeholder
                Rectangle placeholder = new Rectangle(CARD_WIDTH * 0.7, CARD_HEIGHT * 0.7);
                placeholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
                
                // Add text with obstacle name
                Text nameText = new Text(obstacle.getName());
                nameText.setFill(Color.WHITE);
                nameText.setFont(Font.font("System", FontWeight.BOLD, 9));
                nameText.setWrappingWidth(CARD_WIDTH * 0.6);
                nameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                
                obstaclePane.getChildren().addAll(placeholder, nameText);
            }
            
            // Add border with color based on result
            Rectangle border = new Rectangle(
                CARD_WIDTH * 0.7 + 4, 
                CARD_HEIGHT * 0.7 + 4,
                Color.TRANSPARENT
            );
            border.setStroke(succeeded ? Color.GREEN : Color.RED);
            border.setStrokeWidth(2);
            obstaclePane.getChildren().add(border);
            
            // Create tooltip with obstacle details
            Tooltip tooltip = createObstacleTooltip(obstacle, succeeded);
            Tooltip.install(obstaclePane, tooltip);
            
            historyBar.getChildren().add(obstaclePane);
        }
        
        // Auto-scroll to the right to show latest entry
        historyScrollPane.setHvalue(1.0);
    }
    
    /**
     * Creates a tooltip for a completed obstacle
     */
    private Tooltip createObstacleTooltip(ObstacleCard obstacle, boolean succeeded) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(50));
        
        VBox content = new VBox(3);
        content.setPadding(new Insets(5));
        content.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        
        Text nameText = new Text(obstacle.getName());
        nameText.setFill(Color.WHITE);
        nameText.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Text typeText = new Text("Type: " + obstacle.getType());
        typeText.setFill(Color.LIGHTBLUE);
        typeText.setFont(Font.font("System", 12));
        
        Text difficultyText = new Text("Difficulty: " + obstacle.getDifficulty());
        difficultyText.setFill(Color.WHITE);
        difficultyText.setFont(Font.font("System", 12));
        
        Text skillsText = new Text("Required Skills: " + String.join(", ", obstacle.getRequiredSkills()));
        skillsText.setFill(Color.WHITE);
        skillsText.setFont(Font.font("System", 12));
        
        Text resultText = new Text("Result: " + (succeeded ? "SUCCEEDED" : "FAILED"));
        resultText.setFill(succeeded ? Color.LIGHTGREEN : Color.LIGHTPINK);
        resultText.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        content.getChildren().addAll(nameText, typeText, difficultyText, skillsText, resultText);
        
        tooltip.setGraphic(content);
        tooltip.setMaxWidth(250);
        
        return tooltip;
    }
    
    /**
     * Creates a panel with a history bar showing completed obstacles
     */
    private VBox createHistoryPanel() {
        VBox historyPanel = new VBox(5);
        historyPanel.setPadding(new Insets(5, 10, 15, 10));
        historyPanel.setAlignment(Pos.CENTER);
        historyPanel.setMaxWidth(750); // Set a max width
        historyPanel.setStyle("-fx-background-color: #0f2537; -fx-background-radius: 8;");
        
        Label historyLabel = new Label("Obstacle History (Current Loop)");
        historyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        historyLabel.setTextFill(Color.WHITE);
        
        // Create scrollable history bar
        historyScrollPane = new ScrollPane();
        historyScrollPane.setPrefHeight(CARD_HEIGHT * 0.8 + 20); // Slightly smaller than cards
        historyScrollPane.setMinWidth(600); // Set a reasonable minimum width
        historyScrollPane.setMaxWidth(700); // Set a maximum width
        historyScrollPane.setFitToHeight(true);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        historyScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        historyScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        historyBar = new HBox(5);
        historyBar.setAlignment(Pos.CENTER_LEFT);
        historyBar.setPadding(new Insets(5));
        
        historyScrollPane.setContent(historyBar);
        
        historyPanel.getChildren().addAll(historyLabel, historyScrollPane);
        
        return historyPanel;
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
}