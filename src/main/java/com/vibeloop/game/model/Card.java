package com.vibeloop.game.model;

/**
 * Represents a card in the game.
 */
public class Card {
    private String id;
    private String name;
    private String description;
    private String stat;
    private String[] compatibleTypes;
    private String imagePath;
    
    public Card(String id, String name, String description, String stat, String[] compatibleTypes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stat = stat;
        this.compatibleTypes = compatibleTypes;
        this.imagePath = "/cards/" + id + ".jpg";
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getStat() {
        return stat;
    }
    
    public String[] getCompatibleTypes() {
        return compatibleTypes;
    }
    
    /**
     * Checks if this card is compatible with the given obstacle type.
     * 
     * @param obstacleType the obstacle type to check against
     * @return true if the card is compatible with the obstacle type, false otherwise
     */
    public boolean isCompatibleWithType(String obstacleType) {
        if (compatibleTypes == null || obstacleType == null) {
            return false;
        }
        
        for (String type : compatibleTypes) {
            if (type.equalsIgnoreCase(obstacleType)) {
                return true;
            }
        }
        
        return false;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 