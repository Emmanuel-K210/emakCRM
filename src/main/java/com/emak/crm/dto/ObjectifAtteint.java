package com.emak.crm.dto;

import java.math.BigDecimal;

public record ObjectifAtteint(
    String objectif,
    boolean atteint,
    BigDecimal progression
) {}