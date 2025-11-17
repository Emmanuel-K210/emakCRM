package com.emak.crm.dto;

import java.math.BigDecimal;

import lombok.Builder;



@Builder
public record LigneDevisResponse(
    Long id,
    Integer quantite,
    BigDecimal prixUnitaireHt,
    BigDecimal tauxTva,
    BigDecimal remise,
    Long idProduit,
    String nomProduit,
    String referenceSku
) {}