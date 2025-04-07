package com.vibeloop.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of obstacle cards.
 */
public class ObstacleDeck {
    private List<ObstacleCard> allCards;
    private List<ObstacleCard> drawPile;
    private List<ObstacleCard> activeObstacles;
    private List<ObstacleCard> defeatedObstacles;
    
    public ObstacleDeck() {
        allCards = new ArrayList<>();
        drawPile = new ArrayList<>();
        activeObstacles = new ArrayList<>();
        defeatedObstacles = new ArrayList<>();
    }
    
    /**
     * Adds an obstacle card to the deck.
     * 
     * @param card the obstacle card to add
     */
    public void addCard(ObstacleCard card) {
        allCards.add(card);
        drawPile.add(card);
    }
    
    /**
     * Adds multiple obstacle cards to the deck.
     * 
     * @param cards the list of obstacle cards to add
     */
    public void addCards(List<ObstacleCard> cards) {
        allCards.addAll(cards);
        drawPile.addAll(cards);
    }
    
    /**
     * Shuffles the draw pile.
     */
    public void shuffle() {
        Collections.shuffle(drawPile);
    }
    
    /**
     * Draws the top obstacle card from the draw pile.
     * 
     * @return the drawn obstacle card, or null if the draw pile is empty
     */
    public ObstacleCard drawObstacle() {
        if (drawPile.isEmpty()) {
            return null;
        }
        
        ObstacleCard drawnCard = drawPile.remove(0);
        activeObstacles.add(drawnCard);
        return drawnCard;
    }
    
    /**
     * Marks an obstacle as defeated and moves it to the defeated pile.
     * 
     * @param obstacle the obstacle to defeat
     */
    public void defeatObstacle(ObstacleCard obstacle) {
        if (activeObstacles.contains(obstacle)) {
            activeObstacles.remove(obstacle);
            defeatedObstacles.add(obstacle);
        }
    }
    
    /**
     * Resets the deck, moving all cards back to the draw pile and shuffling.
     */
    public void resetDeck() {
        drawPile.clear();
        drawPile.addAll(allCards);
        activeObstacles.clear();
        defeatedObstacles.clear();
        shuffle();
    }
    
    /**
     * Clears active and defeated obstacles without affecting the draw pile.
     * Used for time loop mechanic to maintain original order.
     */
    public void clearObstacles() {
        activeObstacles.clear();
        defeatedObstacles.clear();
    }
    
    /**
     * Gets all obstacle cards in the deck.
     * 
     * @return the list of all obstacle cards
     */
    public List<ObstacleCard> getAllCards() {
        return Collections.unmodifiableList(allCards);
    }
    
    /**
     * Gets the obstacle cards in the draw pile.
     * 
     * @return the list of obstacle cards in the draw pile
     */
    public List<ObstacleCard> getDrawPile() {
        return Collections.unmodifiableList(drawPile);
    }
    
    /**
     * Gets the active obstacle cards.
     * 
     * @return the list of active obstacle cards
     */
    public List<ObstacleCard> getActiveObstacles() {
        return Collections.unmodifiableList(activeObstacles);
    }
    
    /**
     * Gets the defeated obstacle cards.
     * 
     * @return the list of defeated obstacle cards
     */
    public List<ObstacleCard> getDefeatedObstacles() {
        return Collections.unmodifiableList(defeatedObstacles);
    }
} 