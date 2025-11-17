package com.emak.crm.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TacheRequest(
    @NotBlank String titre,
    String description,
    LocalDateTime dateDebut,
    LocalDateTime dateEcheance,
    String priorite,
    String statut,
    @NotNull Long idUtilisateur,
    Long idClient,
    Long idOpportunite
) {}