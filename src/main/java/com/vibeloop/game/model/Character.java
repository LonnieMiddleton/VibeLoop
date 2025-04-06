package com.vibeloop.game.model;

/**
 * Represents a character in the game with various statistics.
 */
public class Character {
    private String type;
    private String name;
    private int strength;
    private int speed;
    private int tech;
    private int health;
    private String description;
    private String imagePath;

    public Character(String type, String name, int strength, int speed, int tech, int health, String description) {
        this.type = type;
        this.name = name;
        this.strength = strength;
        this.speed = speed;
        this.tech = tech;
        this.health = health;
        this.description = description;
        this.imagePath = "/characters/" + type + ".jpg";
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStrength() {
        return strength;
    }

    public int getSpeed() {
        return speed;
    }

    public int getTech() {
        return tech;
    }

    public int getHealth() {
        return health;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return name;
    }
} 