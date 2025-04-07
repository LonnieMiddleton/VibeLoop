package com.vibeloop.game.service;

import com.vibeloop.game.model.Card;
import com.vibeloop.game.model.Deck;

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
 * Service for managing cards.
 */
public class CardService {
    private Map<String, Card> cards;
    private Map<String, List<String>> starterDecks;
    
    public CardService() {
        cards = new HashMap<>();
        starterDecks = new HashMap<>();
        loadCards();
        loadStarterDecks();
    }
    
    /**
     * Loads card data from the JSON configuration file.
     */
    private void loadCards() {
        try (InputStream is = getClass().getResourceAsStream("/cards/cards.json");
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            JsonObject jsonObject = reader.readObject();
            
            for (String id : jsonObject.keySet()) {
                JsonObject cardObj = jsonObject.getJsonObject(id);
                
                // Get compatible types if present, or default to all types if not
                String[] compatibleTypes;
                if (cardObj.containsKey("compatibleTypes")) {
                    JsonArray typesArray = cardObj.getJsonArray("compatibleTypes");
                    compatibleTypes = new String[typesArray.size()];
                    for (int i = 0; i < typesArray.size(); i++) {
                        compatibleTypes[i] = typesArray.getString(i);
                    }
                } else {
                    // Default to all types if not specified
                    compatibleTypes = new String[]{"barrier", "hazard", "environment", "personnel"};
                }
                
                Card card = new Card(
                    id,
                    cardObj.getString("name"),
                    cardObj.getString("description"),
                    cardObj.getString("stat"),
                    compatibleTypes
                );
                cards.put(id, card);
            }
        } catch (Exception e) {
            System.err.println("Error loading card data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads starter deck configurations from the JSON file.
     */
    private void loadStarterDecks() {
        try (InputStream is = getClass().getResourceAsStream("/cards/starter_decks.json");
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            JsonObject jsonObject = reader.readObject();
            
            for (String characterType : jsonObject.keySet()) {
                JsonObject deckObj = jsonObject.getJsonObject(characterType);
                JsonArray cardIds = deckObj.getJsonArray("cards");
                
                List<String> deckCards = new ArrayList<>();
                for (int i = 0; i < cardIds.size(); i++) {
                    deckCards.add(cardIds.getString(i));
                }
                
                starterDecks.put(characterType, deckCards);
            }
        } catch (Exception e) {
            System.err.println("Error loading starter deck data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a card by its ID.
     * 
     * @param id the card ID
     * @return the card
     */
    public Card getCard(String id) {
        return cards.get(id);
    }
    
    /**
     * Gets all cards.
     * 
     * @return the map of cards
     */
    public Map<String, Card> getAllCards() {
        return cards;
    }
    
    /**
     * Creates a starter deck for a character type.
     * 
     * @param characterType the character type
     * @return the starter deck
     */
    public Deck createStarterDeck(String characterType) {
        Deck deck = new Deck();
        List<String> cardIds = starterDecks.get(characterType);
        
        if (cardIds != null) {
            for (String cardId : cardIds) {
                Card card = getCard(cardId);
                if (card != null) {
                    deck.addCard(card);
                }
            }
        }
        
        deck.shuffle();
        return deck;
    }
} 