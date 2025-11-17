package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FactureRequest(
    @NotBlank String numeroFacture,
    @NotNull LocalDate dateFacture,
    LocalDate dateEcheance,
    BigDecimal montantHt,
    BigDecimal montantTtc,
    BigDecimal tauxTva,
    BigDecimal montantRestant,
    String statut,
    Long idDevis,
    @NotNull Long idClient,
    @NotNull Long idUtilisateur
) {}