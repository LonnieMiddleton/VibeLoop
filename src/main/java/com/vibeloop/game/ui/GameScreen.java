package com.vibeloop.game.ui;

import com.vibeloop.game.model.Card;
import com.vibeloop.game.model.Player;
import com.vibeloop.game.model.Character;
import com.vibeloop.game.service.ObstacleService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Main game screen showing player profiles, decks, discard piles, and hands.
 */
public class GameScreen {
    private final Stage stage;
    private final List<Player> players;
    private final ObstacleService obstacleService;
    
    // UI constants
    private static final double PROFILE_WIDTH = 80;
    private static final double CARD_WIDTH = 60;
    private static final double CARD_HEIGHT = 63; // Reduced by ~30%
    private static final double CARD_SPACING = 3;
    
    public GameScreen(Stage stage, List<Player> players, ObstacleService obstacleService) {
        this.stage = stage;
        this.players = players;
        this.obstacleService = obstacleService;
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
        
        // Create center panel for game board/obstacles (will be implemented later)
        VBox centerPanel = new VBox(10);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setPadding(new Insets(10));
        
        Label centerLabel = new Label("Game Board");
        centerLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        centerLabel.setTextFill(Color.WHITE);
        centerPanel.getChildren().add(centerLabel);
        
        root.setCenter(centerPanel);
        
        // Create scene and show
        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("VibeLoop Game");
        stage.setScene(scene);
    }
    
    /**
     * Initializes players' hands by drawing 5 cards for each player.
     */
    private void initializePlayersHands() {
        for (Player player : players) {
            player.getDeck().shuffle();
            player.getDeck().drawCards(5);
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
        
        // Player name and character
        Label nameLabel = new Label(player.getName() + " - " + player.getSelectedCharacter().getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        
        // Main row with character image and hand
        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        
        // Character image
        ImageView characterImage = new ImageView(new Image(player.getSelectedCharacter().getImagePath()));
        characterImage.setFitWidth(PROFILE_WIDTH);
        characterImage.setFitHeight(PROFILE_WIDTH);
        characterImage.setPreserveRatio(true);
        
        // Create tooltip for character stats
        Tooltip characterTooltip = createCharacterTooltip(player.getSelectedCharacter());
        Tooltip.install(characterImage, characterTooltip);
        
        // Hand of cards
        VBox handBox = new VBox(3);
        
        FlowPane handPane = new FlowPane();
        handPane.setHgap(CARD_SPACING);
        handPane.setVgap(CARD_SPACING);
        handPane.setPrefWidth(PROFILE_WIDTH + 140);
        
        for (Card card : player.getDeck().getHand()) {
            ImageView cardImage;
            String cardId = card.getId();
            
            // Try all possible paths for the card image
            String[] possiblePaths = {
                "/cards/" + cardId + ".jpg",
                "/cards/skills/" + cardId + ".jpg", 
                "/cards/tools/" + cardId + ".jpg"
            };
            
            boolean imageLoaded = false;
            for (String path : possiblePaths) {
                try {
                    Image image = new Image(getClass().getResourceAsStream(path));
                    cardImage = new ImageView(image);
                    cardImage.setFitWidth(CARD_WIDTH);
                    cardImage.setFitHeight(CARD_HEIGHT);
                    
                    // Create tooltip
                    Tooltip tooltip = createCardTooltip(card);
                    tooltip.setShowDelay(Duration.millis(50));
                    Tooltip.install(cardImage, tooltip);
                    
                    handPane.getChildren().add(cardImage);
                    imageLoaded = true;
                    System.out.println("Loaded image for card: " + cardId + " from path: " + path);
                    break;
                } catch (Exception e) {
                    // Try next path
                    continue;
                }
            }
            
            // If no image was loaded, use card back
            if (!imageLoaded) {
                System.out.println("Could not load image for card: " + cardId + " - using card back");
                try {
                    Image backImage = new Image(getClass().getResourceAsStream("/cards/card_back.jpg"));
                    cardImage = new ImageView(backImage);
                    cardImage.setFitWidth(CARD_WIDTH);
                    cardImage.setFitHeight(CARD_HEIGHT);
                    
                    // Create tooltip
                    Tooltip tooltip = createCardTooltip(card);
                    tooltip.setShowDelay(Duration.millis(50));
                    Tooltip.install(cardImage, tooltip);
                    
                    handPane.getChildren().add(cardImage);
                } catch (Exception e) {
                    System.err.println("Error loading card back image: " + e.getMessage());
                }
            }
        }
        
        handBox.getChildren().add(handPane);
        
        // Add character image and hand to main row
        mainRow.getChildren().addAll(characterImage, handBox);
        
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
        
        Rectangle discardPlaceholder = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        discardPlaceholder.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
        discardPlaceholder.setStroke(Color.WHITE);
        
        Label discardLabel = new Label("Discard: " + player.getDeck().getDiscardPile().size());
        discardLabel.setTextFill(Color.WHITE);
        discardLabel.setFont(Font.font("System", 11));
        
        discardPileBox.getChildren().addAll(discardPlaceholder, discardLabel);
        
        deckRow.getChildren().addAll(drawPileBox, discardPileBox);
        
        // Add all sections
        playerSection.getChildren().addAll(nameLabel, mainRow, deckRow);
        
        return playerSection;
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
        
        Text typeText = new Text("Type: " + card.getType());
        typeText.setFill(Color.WHITE);
        typeText.setFont(Font.font("System", 12));
        
        Text costText = new Text("Cost: " + card.getCost());
        costText.setFill(Color.WHITE);
        costText.setFont(Font.font("System", 12));
        
        Text descriptionText = new Text("Description: " + card.getDescription());
        descriptionText.setFill(Color.WHITE);
        descriptionText.setFont(Font.font("System", 12));
        descriptionText.setWrappingWidth(250);
        
        Text effectText = new Text("Effect: " + card.getEffect());
        effectText.setFill(Color.WHITE);
        effectText.setFont(Font.font("System", 12));
        effectText.setWrappingWidth(250);
        
        content.getChildren().addAll(nameText, typeText, costText, descriptionText, effectText);
        
        tooltip.setGraphic(content);
        tooltip.setMaxWidth(280);
        
        return tooltip;
    }
}