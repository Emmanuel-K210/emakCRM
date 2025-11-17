package com.emak.crm.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProduitRequest(
    @NotBlank String referenceSku,
    @NotBlank String nomProduit,
    String description,
    String categorie,
    String famille,
    @NotNull BigDecimal prixUnitaireHt,
    BigDecimal coutUnitaire,
    Integer stock
) {}