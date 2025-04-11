package com.vibeloop.game.service;

import com.vibeloop.game.model.ObstacleCard;
import com.vibeloop.game.model.ObstacleDeck;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Service for managing obstacle cards.
 */
public class ObstacleService {
    private Map<String, ObstacleCard> obstacleCards;
    private GameConfigService configService;
    
    public ObstacleService() {
        obstacleCards = new HashMap<>();
        configService = new GameConfigService();
        loadObstacleCards();
    }
    
    /**
     * Loads obstacle card data from the JSON configuration file.
     */
    private void loadObstacleCards() {
        try (InputStream is = getClass().getResourceAsStream("/obstacles/obstacles.json");
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            JsonObject jsonObject = reader.readObject();
            
            for (String id : jsonObject.keySet()) {
                JsonObject obstacleObj = jsonObject.getJsonObject(id);
                JsonArray skillsArray = obstacleObj.getJsonArray("requiredSkills");
                
                String[] requiredSkills = new String[skillsArray.size()];
                for (int i = 0; i < skillsArray.size(); i++) {
                    requiredSkills[i] = skillsArray.getString(i);
                }
                
                ObstacleCard obstacle = new ObstacleCard(
                    id,
                    obstacleObj.getString("name"),
                    obstacleObj.getString("description"),
                    obstacleObj.getInt("difficulty"),
                    requiredSkills,
                    obstacleObj.getString("type")
                );
                obstacleCards.put(id, obstacle);
            }
        } catch (Exception e) {
            System.err.println("Error loading obstacle card data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets an obstacle card by its ID.
     * 
     * @param id the obstacle card ID
     * @return the obstacle card
     */
    public ObstacleCard getObstacleCard(String id) {
        return obstacleCards.get(id);
    }
    
    /**
     * Gets all obstacle cards.
     * 
     * @return the map of obstacle cards
     */
    public Map<String, ObstacleCard> getAllObstacleCards() {
        return obstacleCards;
    }
    
    /**
     * Creates a new obstacle deck with all available obstacle cards.
     * 
     * @return the obstacle deck
     */
    public ObstacleDeck createObstacleDeck() {
        ObstacleDeck deck = new ObstacleDeck();
        
        // Find the finale card (nuclear core)
        ObstacleCard finaleCard = null;
        for (ObstacleCard card : obstacleCards.values()) {
            if (card.isFinale()) {
                finaleCard = card;
                break;
            }
        }
        
        // Get all obstacle cards except the finale
        List<ObstacleCard> regularCards = new ArrayList<>();
        for (ObstacleCard card : obstacleCards.values()) {
            if (!card.isFinale()) {
                regularCards.add(card);
            }
        }
        
        // Get deck size from config (minus 1 for the finale card)
        int deckSize = configService.getObstacleDeckSize() - 1;
        
        // If deckSize is less than total regular cards, randomly select cards
        if (deckSize < regularCards.size()) {
            // Shuffle the list to get random selection
            Collections.shuffle(regularCards);
            // Take the first 'deckSize' cards
            regularCards = regularCards.subList(0, deckSize);
        }
        
        // Shuffle regular cards if configured to do so
        if (configService.shouldShuffleObstacleDeck()) {
            Collections.shuffle(regularCards);
        }
        
        // Add regular cards to the deck
        for (ObstacleCard card : regularCards) {
            deck.addCard(card);
        }
        
        // Add the finale card as the last obstacle if it exists
        if (finaleCard != null) {
            deck.addCard(finaleCard);
        }
        
        System.out.println("Created deck with " + deck.getAllCards().size() + " cards" + 
                          (finaleCard != null ? " (including finale: " + finaleCard.getName() + ")" : ""));
        
        return deck;
    }
    
    /**
     * Creates a difficulty-specific obstacle deck.
     * 
     * @param difficulty the maximum difficulty level to include
     * @return the obstacle deck with filtered cards
     */
    public ObstacleDeck createObstacleDeck(int difficulty) {
        ObstacleDeck deck = new ObstacleDeck();
        
        // Find the finale card (nuclear core)
        ObstacleCard finaleCard = null;
        for (ObstacleCard card : obstacleCards.values()) {
            if (card.isFinale()) {
                finaleCard = card;
                break;
            }
        }
        
        // Get all obstacle cards that match the difficulty level or lower, except the finale
        List<ObstacleCard> matchingCards = new ArrayList<>();
        for (ObstacleCard card : obstacleCards.values()) {
            if (!card.isFinale() && card.getDifficulty() <= difficulty) {
                matchingCards.add(card);
            }
        }
        
        // Get deck size from config (minus 1 for the finale card)
        int deckSize = configService.getObstacleDeckSize() - 1;
        
        // If deckSize is less than matching cards, randomly select cards
        if (deckSize < matchingCards.size()) {
            // Shuffle the list to get random selection
            Collections.shuffle(matchingCards);
            // Take the first 'deckSize' cards
            matchingCards = matchingCards.subList(0, deckSize);
        }
        
        // Shuffle matching cards if configured to do so
        if (configService.shouldShuffleObstacleDeck()) {
            Collections.shuffle(matchingCards);
        }
        
        // Add matching cards to the deck
        for (ObstacleCard card : matchingCards) {
            deck.addCard(card);
        }
        
        // Add the finale card as the last obstacle if it exists
        if (finaleCard != null) {
            deck.addCard(finaleCard);
        }
        
        System.out.println("Created difficulty-filtered deck with " + deck.getAllCards().size() + " cards" + 
                          (finaleCard != null ? " (including finale: " + finaleCard.getName() + ")" : ""));
        
        return deck;
    }
}
