package com.emak.crm.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record InteractionResponse(
    Long idInteraction,
    String type,
    String objet,
    String compteRendu,
    String resultat,
    LocalDateTime dateInteraction,
    Integer duree,
    Long idClient,
    String nomClient,
    Long idUtilisateur,
    String nomUtilisateur,
    Long idOpportunite,
    String nomOpportunite,
    LocalDateTime dateCreation
) {}