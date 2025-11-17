package com.emak.crm.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteInterneRequest(
    @NotBlank(message = "Le titre est obligatoire")
    String titre,
    
    @NotBlank(message = "Le contenu est obligatoire")
    String contenu,
    
    @NotBlank(message = "Le type est obligatoire")
    String type,
    
    Boolean privee,
    
    Long idClient,
    
    Long idOpportunite
) {}