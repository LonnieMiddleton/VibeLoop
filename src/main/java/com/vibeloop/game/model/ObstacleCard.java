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
    private boolean isFinale;
    
    // Special requirements for the finale
    private int environmentRequired;
    private int hazardRequired;
    private int barrierRequired;
    
    public ObstacleCard(String id, String name, String description, int difficulty, 
                      String[] requiredSkills, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.requiredSkills = requiredSkills;
        this.type = type;
        this.imagePath = "/obstacles/" + id + ".jpg";
        this.isFinale = "finale".equals(type);
        
        // Default values for finale requirements
        if (isFinale) {
            this.environmentRequired = 5;
            this.hazardRequired = 5;
            this.barrierRequired = 5;
        }
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
    
    public boolean isFinale() {
        return isFinale;
    }
    
    /**
     * For the finale obstacle, checks if a card is compatible regardless of type.
     * For regular obstacles, uses the standard compatibility check.
     * 
     * @param card the card to check
     * @return true if the card is compatible with this obstacle
     */
    public boolean isCardCompatible(Card card) {
        if (isFinale) {
            // For finale, all card types are accepted
            return true;
        } else {
            // For regular obstacles, check normal compatibility
            return card.isCompatibleWithType(this.type);
        }
    }
    
    /**
     * Gets the environment card requirement for the finale.
     * 
     * @return the environment cards required
     */
    public int getEnvironmentRequired() {
        return environmentRequired;
    }
    
    /**
     * Gets the hazard card requirement for the finale.
     * 
     * @return the hazard cards required
     */
    public int getHazardRequired() {
        return hazardRequired;
    }
    
    /**
     * Gets the barrier card requirement for the finale.
     * 
     * @return the barrier cards required
     */
    public int getBarrierRequired() {
        return barrierRequired;
    }
    
    /**
     * Determines the card's contribution based on its compatibility.
     * For regular obstacles, this is based on skill matching.
     * For the finale, cards contribute to their specific type requirement.
     * 
     * @param card The card being played
     * @param player The player playing the card
     * @return the amount that the card contributes to its requirement
     */
    public int getCardContribution(Card card, Player player) {
        if (isFinale) {
            // For finale, contribution is based on the player's relevant stat
            String stat = card.getStat().toLowerCase();
            Character character = player.getSelectedCharacter();
            
            switch (stat) {
                case "strength":
                    return character.getStrength();
                case "speed":
                    return character.getSpeed();
                case "tech":
                    return character.getTech();
                default:
                    return 1;
            }
        } else {
            // For regular obstacles, check if stats match
            String cardStat = card.getStat().toLowerCase();
            Character character = player.getSelectedCharacter();
            
            for (String requiredSkill : requiredSkills) {
                if (cardStat.equals(requiredSkill.toLowerCase())) {
                    switch (cardStat) {
                        case "strength":
                            return character.getStrength();
                        case "speed":
                            return character.getSpeed();
                        case "tech":
                            return character.getTech();
                        default:
                            return 1;
                    }
                }
            }
            
            // Card doesn't match a required skill, contributes base value of 1
            return 1;
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
} 