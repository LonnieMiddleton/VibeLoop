package com.vibeloop.game.service;

import com.vibeloop.game.model.Character;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Service for managing character data.
 */
public class CharacterService {
    private Map<String, Character> characters;
    private List<String> characterTypes;

    public CharacterService() {
        characters = new HashMap<>();
        characterTypes = new ArrayList<>();
        loadCharacters();
    }

    /**
     * Loads character data from the JSON configuration file.
     */
    private void loadCharacters() {
        try (InputStream is = getClass().getResourceAsStream("/characters/stats.json");
             JsonReader reader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            JsonObject jsonObject = reader.readObject();
            
            for (String type : jsonObject.keySet()) {
                JsonObject charObj = jsonObject.getJsonObject(type);
                Character character = new Character(
                    type,
                    charObj.getString("name"),
                    charObj.getInt("strength"),
                    charObj.getInt("speed"),
                    charObj.getInt("tech"),
                    charObj.getInt("health"),
                    charObj.getString("description")
                );
                characters.put(type, character);
                characterTypes.add(type);
            }
        } catch (Exception e) {
            System.err.println("Error loading character data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets a character by its type.
     * 
     * @param type the character type
     * @return the character
     */
    public Character getCharacter(String type) {
        return characters.get(type);
    }

    /**
     * Gets all character types.
     * 
     * @return the list of character types
     */
    public List<String> getCharacterTypes() {
        return characterTypes;
    }

    /**
     * Gets the next character type in the list.
     * 
     * @param currentType the current character type
     * @return the next character type
     */
    public String getNextCharacterType(String currentType) {
        int index = characterTypes.indexOf(currentType);
        return characterTypes.get((index + 1) % characterTypes.size());
    }

    /**
     * Gets the previous character type in the list.
     * 
     * @param currentType the current character type
     * @return the previous character type
     */
    public String getPreviousCharacterType(String currentType) {
        int index = characterTypes.indexOf(currentType);
        return characterTypes.get((index - 1 + characterTypes.size()) % characterTypes.size());
    }
} 