package com.vibeloop.game.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents a player in the game.
 */
public class Player {
    private final int playerNumber;
    private final StringProperty name = new SimpleStringProperty("Player");
    private Character selectedCharacter;
    private Deck deck;
    private final IntegerProperty currentHealth = new SimpleIntegerProperty(0);

    public Player(int playerNumber, Character initialCharacter) {
        this.playerNumber = playerNumber;
        this.name.set("Player " + playerNumber);
        this.selectedCharacter = initialCharacter;
        // Initialize health to character's max health
        if (initialCharacter != null) {
            this.currentHealth.set(initialCharacter.getHealth());
        }
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
        // Reset health when character changes
        this.currentHealth.set(selectedCharacter.getHealth());
    }
    
    public Deck getDeck() {
        return deck;
    }
    
    public void setDeck(Deck deck) {
        this.deck = deck;
    }
    
    /**
     * Gets the current health of the player.
     */
    public int getCurrentHealth() {
        return currentHealth.get();
    }
    
    /**
     * Gets the health property for binding.
     */
    public IntegerProperty currentHealthProperty() {
        return currentHealth;
    }
    
    /**
     * Takes damage, reducing the player's health.
     * 
     * @param amount The amount of damage to take
     * @return Remaining health after taking damage
     */
    public int takeDamage(int amount) {
        int newHealth = Math.max(0, getCurrentHealth() - amount);
        currentHealth.set(newHealth);
        return newHealth;
    }
    
    /**
     * Checks if the player is defeated (health <= 0).
     * 
     * @return true if the player is defeated, false otherwise
     */
    public boolean isDefeated() {
        return getCurrentHealth() <= 0;
    }
    
    /**
     * Heals the player, increasing their health up to their maximum.
     * 
     * @param amount The amount to heal
     * @return The new health value
     */
    public int heal(int amount) {
        int maxHealth = selectedCharacter.getHealth();
        int newHealth = Math.min(maxHealth, getCurrentHealth() + amount);
        currentHealth.set(newHealth);
        return newHealth;
    }
} 