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
} 