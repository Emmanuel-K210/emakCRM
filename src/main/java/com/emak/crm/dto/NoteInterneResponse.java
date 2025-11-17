package com.emak.crm.dto;

import java.time.LocalDateTime;

public record NoteInterneResponse(
    Long id,
    String titre,
    String contenu,
    String type,
    Boolean privee,
    Long idAuteur,
    String nomAuteur,
    String prenomAuteur,
    Long idClient,
    String nomClient,
    String entrepriseClient,
    Long idOpportunite,
    String nomOpportunite,
    LocalDateTime dateCreation,
    LocalDateTime dateModification
) {}