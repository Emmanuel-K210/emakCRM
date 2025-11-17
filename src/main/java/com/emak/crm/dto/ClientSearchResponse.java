package com.emak.crm.dto;

import java.util.List;
import java.util.Map;

public record ClientSearchResponse(
    List<ClientResponse> clients,
    int pageCourante,
    int totalPages,
    long totalElements,
    int taillePage,
    String searchTerm,
    Map<String, Object> statistiques,
    List<String> suggestions
) {
    
    public ClientSearchResponse {
        if (statistiques == null) {
            statistiques = Map.of();
        }
        if (suggestions == null) {
            suggestions = List.of();
        }
    }
    
    // Méthodes utilitaires pour la pagination
    public boolean hasPrevious() {
        return pageCourante > 1;
    }
    
    public boolean hasNext() {
        return pageCourante < totalPages;
    }
    
    public int getPagePrecedente() {
        return hasPrevious() ? pageCourante - 1 : 1;
    }
    
    public int getPageSuivante() {
        return hasNext() ? pageCourante + 1 : totalPages;
    }
}