package com.emak.crm.dto;

import java.util.List;

public record SuggestionResponse(
    String term,
    List<String> suggestions,
    int totalSuggestions,
    List<String> categories
) {
    
    public SuggestionResponse {
        if (suggestions == null) suggestions = List.of();
        if (categories == null) categories = List.of();
    }
}