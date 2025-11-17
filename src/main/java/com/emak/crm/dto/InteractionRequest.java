package com.emak.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record InteractionRequest(
    @NotBlank String type,
    @NotBlank String objet,
    String compteRendu,
    String resultat,
    @NotNull LocalDateTime dateInteraction,
    Integer duree,
    @NotNull Long idClient,
    @NotNull Long idUtilisateur,
    Long idOpportunite
) {}