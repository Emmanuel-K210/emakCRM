package com.emak.crm.dto;

import java.time.LocalDateTime;

import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record ClientSearchRequest(
    // Recherche texte libre sur multiples champs
    String searchTerm,
    
    // Filtres par type et statut
    TypeClient typeClient,
    StatutClient statut,
    
    // Filtre géographique
    String ville,
    String codePostal,
    String pays,
    
    // Filtre par score de prospect
    @Min(value = 1, message = "Le score minimum doit être entre 1 et 10")
    @Max(value = 10, message = "Le score maximum doit être entre 1 et 10")
    Integer scoreMin,
    
    @Min(value = 1, message = "Le score minimum doit être entre 1 et 10")
    @Max(value = 10, message = "Le score maximum doit être entre 1 et 10")
    Integer scoreMax,
    
    // Filtre par commercial responsable
    Long idUtilisateurResponsable,
    
    // Filtre par origine du prospect
    OrigineClient origine,
    
    // Dates de création
    LocalDateTime dateCreationDebut,
    LocalDateTime dateCreationFin,
    
    // Pagination et tri
    @Min(value = 1, message = "Le numéro de page doit être au moins 1")
    Integer page,
    
    @Min(value = 1, message = "La taille de page doit être au moins 1")
    @Max(value = 100, message = "La taille de page ne peut pas dépasser 100")
    Integer size,
    
    String triPar,
    String ordreTri
) {
    
    // Constructeur par défaut avec valeurs par défaut
    public ClientSearchRequest {
        if (page == null) page = 1;
        if (size == null) size = 20;
        if (triPar == null) triPar = "nom";
        if (ordreTri == null) ordreTri = "ASC";
    }


	// Méthode utilitaire pour obtenir l'offset de pagination
    public int getOffset() {
        return (page - 1) * size;
    }
}