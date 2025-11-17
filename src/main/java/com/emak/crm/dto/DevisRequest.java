package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DevisRequest(
    @NotBlank String numeroDevis,
    @NotNull LocalDate dateEmission,
    LocalDate dateValidite,
    BigDecimal montantHt,
    BigDecimal montantTtc,
    BigDecimal tauxTva,
    String statut,
    @NotNull Long idOpportunite,
    @NotNull Long idUtilisateur,
    List<LigneDevisRequest> lignes
) {}