package com.vibeloop.game.model;

/**
 * Represents an obstacle card in the game that players must overcome.
 */
public class ObstacleCard {
    private String id;
    private String name;
    private String description;
    private int difficulty;
    private String[] requiredSkills;
    private String type;
    private String imagePath;
    
    public ObstacleCard(String id, String name, String description, int difficulty, 
                      String[] requiredSkills, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.requiredSkills = requiredSkills;
        this.type = type;
        this.imagePath = "/obstacles/" + id + ".jpg";
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
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public String[] getRequiredSkills() {
        return requiredSkills;
    }
    
    public String getType() {
        return type;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 