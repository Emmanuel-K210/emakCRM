package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpportuniteRequest(
    @NotBlank String nomOpportunite,
    String etapeVente,
    @NotNull Integer probabilite,
    String statut,
    BigDecimal montantEstime,
    LocalDate dateCloturePrevue,
    String description,
    String source,
    @NotNull Long idClient,
    @NotNull Long idUtilisateur
) {}