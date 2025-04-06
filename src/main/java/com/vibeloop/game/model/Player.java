package com.vibeloop.game.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a player in the game.
 */
public class Player {
    private final int playerNumber;
    private final StringProperty name = new SimpleStringProperty("Player");
    private Character selectedCharacter;
    private Deck deck;

    public Player(int playerNumber, Character initialCharacter) {
        this.playerNumber = playerNumber;
        this.name.set("Player " + playerNumber);
        this.selectedCharacter = initialCharacter;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Character getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(Character selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }
    
    public Deck getDeck() {
        return deck;
    }
    
    public void setDeck(Deck deck) {
        this.deck = deck;
    }
} 