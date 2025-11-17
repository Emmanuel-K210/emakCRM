package com.emak.crm.dto;

import java.math.BigDecimal;

public record ComparaisonMensuelle(
    String mois,
    BigDecimal chiffreAffaires,
    BigDecimal objectif,
    BigDecimal ecart
) {}