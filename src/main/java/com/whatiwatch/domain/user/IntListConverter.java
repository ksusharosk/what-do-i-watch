package com.whatiwatch.domain.user;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntListConverter implements AttributeConverter<List<Integer>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        try { return MAPPER.writeValueAsString(list); }
        catch (Exception e) { throw new IllegalStateException("Failed to serialize genre ids", e); }
    }

    @Override
    public List<Integer> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return MAPPER.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Integer>>() {}); }
        catch (Exception e) { throw new IllegalStateException("Failed to deserialize genre ids", e); }
    }
}
