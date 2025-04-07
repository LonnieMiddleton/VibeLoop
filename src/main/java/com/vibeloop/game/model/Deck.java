package com.vibeloop.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of cards.
 */
public class Deck {
    private List<Card> cards;
    private List<Card> drawPile;
    private List<Card> hand;
    private List<Card> discardPile;
    
    public Deck() {
        cards = new ArrayList<>();
        drawPile = new ArrayList<>();
        hand = new ArrayList<>();
        discardPile = new ArrayList<>();
    }
    
    public void addCard(Card card) {
        cards.add(card);
        drawPile.add(card);
    }
    
    public void addCards(List<Card> newCards) {
        cards.addAll(newCards);
        drawPile.addAll(newCards);
    }
    
    public void shuffle() {
        Collections.shuffle(drawPile);
    }
    
    public Card drawCard() {
        if (drawPile.isEmpty()) {
            // Don't automatically shuffle the discard pile back into the draw pile
            // Just return null if there are no more cards to draw
            return null;
        }
        
        Card drawnCard = drawPile.remove(0);
        hand.add(drawnCard);
        return drawnCard;
    }
    
    public void drawCards(int count) {
        for (int i = 0; i < count; i++) {
            Card drawnCard = drawCard();
            if (drawnCard == null) {
                break; // No more cards to draw
            }
        }
    }
    
    public void playCard(Card card) {
        if (hand.contains(card)) {
            hand.remove(card);
            discardPile.add(card);
        }
    }
    
    public void discardHand() {
        discardPile.addAll(hand);
        hand.clear();
    }
    
    private void resetDrawPile() {
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }
    
    /**
     * Resets the deck for the time loop mechanic.
     * Moves all cards from hand and discard pile back to the draw pile.
     */
    public void resetDeckForTimeLoop() {
        // Get the main collection of all remaining cards (after any removals)
        List<Card> allRemainingCards = new ArrayList<>(cards);
        
        // Clear everything
        drawPile.clear();
        hand.clear();
        discardPile.clear();
        
        // Put all remaining cards into the draw pile
        drawPile.addAll(allRemainingCards);
        
        // Shuffle the deck
        shuffle();
        
        System.out.println("Reset deck complete. Total cards: " + cards.size() + 
                          ", Draw pile: " + drawPile.size() + 
                          ", Hand: " + hand.size() + 
                          ", Discard: " + discardPile.size());
    }
    
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
    
    public List<Card> getDrawPile() {
        return Collections.unmodifiableList(drawPile);
    }
    
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }
    
    public List<Card> getDiscardPile() {
        return Collections.unmodifiableList(discardPile);
    }
    
    /**
     * Adds a card to the deck and discard pile.
     * 
     * @param card the card to add
     */
    public void addCardToDiscard(Card card) {
        cards.add(card);
        discardPile.add(card);
    }
    
    /**
     * Removes a specific card from the deck completely.
     * 
     * @param card the card to remove
     * @return true if the card was removed, false if it wasn't found
     */
    public boolean removeCard(Card card) {
        boolean removedFromMain = cards.remove(card);
        
        // Also remove from all possible locations
        boolean removedFromDraw = drawPile.remove(card);
        boolean removedFromHand = hand.remove(card);
        boolean removedFromDiscard = discardPile.remove(card);
        
        return removedFromMain || removedFromDraw || removedFromHand || removedFromDiscard;
    }
} 