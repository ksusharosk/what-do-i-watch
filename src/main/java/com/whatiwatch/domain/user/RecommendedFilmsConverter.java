package com.whatiwatch.domain.user;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RecommendedFilmsConverter
        implements AttributeConverter<List<RecommendationHistoryEntry.RecommendedFilm>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<RecommendationHistoryEntry.RecommendedFilm> films) {
        if (films == null || films.isEmpty()) return null;
        try { return MAPPER.writeValueAsString(films); }
        catch (Exception e) { throw new IllegalStateException("Failed to serialize films", e); }
    }

    @Override
    public List<RecommendationHistoryEntry.RecommendedFilm> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return MAPPER.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("Failed to deserialize films", e); }
    }
}