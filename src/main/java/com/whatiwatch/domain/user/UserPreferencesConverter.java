package com.whatiwatch.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts UserPreferences to/from a JSON string for satabase storage
 */
@Converter
public class UserPreferencesConverter implements AttributeConverter<UserPreferences, String> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // UserPreferences -> JSON string
    @Override
    public String convertToDatabaseColumn(UserPreferences preferences) {
        if (preferences == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(preferences);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize UserPreferences", e);
        }
    }

    // JSON string -> UserPreferences
    @Override
    public UserPreferences convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return UserPreferences.defaults();
        }
        try {
            return MAPPER.readValue(json, UserPreferences.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize UserPreferences", e);
        }
    }

}
