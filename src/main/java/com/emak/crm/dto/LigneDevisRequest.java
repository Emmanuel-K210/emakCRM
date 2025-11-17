package com.emak.crm.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record LigneDevisRequest(
    @NotNull Long idProduit,
    @NotNull Integer quantite,
    @NotNull String description,
    @NotNull BigDecimal prixUnitaireHt,
    BigDecimal tauxTva,
    BigDecimal remise
) {}