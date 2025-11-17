package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ProduitResponse(
    Long id,
    String referenceSku,
    String nomProduit,
    String description,
    String categorie,
    String famille,
    BigDecimal prixUnitaireHt,
    BigDecimal coutUnitaire,
    Integer stock,
    Boolean actif,
    LocalDateTime dateCreation
) {}