package com.vibeloop.game.service;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Service for managing game configuration settings.
 */
public class GameConfigService {
    private JsonObject config;
    
    public GameConfigService() {
        loadConfig();
    }
    
    /**
     * Loads game configuration from the JSON file.
     */
    private void loadConfig() {
        try (InputStream is = getClass().getResourceAsStream("/game_config.json");
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            config = reader.readObject();
        } catch (Exception e) {
            System.err.println("Error loading game configuration: " + e.getMessage());
            e.printStackTrace();
            // Create default config if loading fails
            config = Json.createObjectBuilder()
                .add("obstacles", Json.createObjectBuilder()
                    .add("deck_size", 12)
                    .add("shuffle", true)
                    .build())
                .build();
        }
    }
    
    /**
     * Gets the maximum number of obstacle cards to include in the deck.
     * 
     * @return the obstacle deck size
     */
    public int getObstacleDeckSize() {
        return config.getJsonObject("obstacles").getInt("deck_size");
    }
    
    /**
     * Checks if the obstacle deck should be shuffled.
     * 
     * @return true if the deck should be shuffled, false otherwise
     */
    public boolean shouldShuffleObstacleDeck() {
        return config.getJsonObject("obstacles").getBoolean("shuffle");
    }
} 