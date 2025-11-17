package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PaiementRequest(
    @NotNull LocalDate datePaiement,
    @NotNull BigDecimal montant,
    @NotNull String modePaiement,
    @NotNull String libelle,
    String referenceTransaction,
    String statut,
    @NotNull Long idFacture
) {}