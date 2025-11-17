package com.emak.crm.dto;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record TacheResponse(
    Long idTache,
    String titre,
    String description,
    LocalDateTime dateDebut,
    LocalDateTime dateEcheance,
    String priorite,
    String statut,
    Long idUtilisateur,
    String nomUtilisateur,
    Long idClient,
    String nomClient,
    Long idOpportunite,
    String nomOpportunite,
    LocalDateTime dateCreation
) {}