package com.emak.crm.dto;

import java.math.BigDecimal;

public record PerformanceMensuelle(
    String mois,
    BigDecimal chiffreAffaires,
    BigDecimal objectif,
    Integer dealsConvertis
) {}