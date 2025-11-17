package com.emak.crm.dto;

import java.math.BigDecimal;

public record VentesParProduit(
    String nomProduit,
    BigDecimal chiffreAffaires,
    Integer quantiteVendue,
    BigDecimal partMarche
) {}
