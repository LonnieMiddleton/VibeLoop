package com.vibeloop.game.service;

import com.vibeloop.game.model.ObstacleCard;
import com.vibeloop.game.model.ObstacleDeck;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    
    public ObstacleService() {
        obstacleCards = new HashMap<>();
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
                    requiredSkills
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
        
        // Add all obstacle cards to the deck
        for (ObstacleCard card : obstacleCards.values()) {
            deck.addCard(card);
        }
        
        deck.shuffle();
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
        
        // Add obstacle cards that match the difficulty level or lower
        for (ObstacleCard card : obstacleCards.values()) {
            if (card.getDifficulty() <= difficulty) {
                deck.addCard(card);
            }
        }
        
        deck.shuffle();
        return deck;
    }
} 