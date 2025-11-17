package com.emak.crm.dto;

import java.math.BigDecimal;

public record VentesParCommercial(
    String nomCommercial,
    BigDecimal chiffreAffaires,
    Integer nombreDeals,
    BigDecimal tauxConversion
) {}