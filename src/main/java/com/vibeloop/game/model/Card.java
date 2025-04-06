package com.vibeloop.game.model;

/**
 * Represents a card in the game.
 */
public class Card {
    private String id;
    private String name;
    private String description;
    private String type;
    private int cost;
    private String effect;
    private String imagePath;
    
    public Card(String id, String name, String description, String type, int cost, String effect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.cost = cost;
        this.effect = effect;
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
    
    public String getType() {
        return type;
    }
    
    public int getCost() {
        return cost;
    }
    
    public String getEffect() {
        return effect;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 