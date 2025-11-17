package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PointDonnees(
    LocalDate date,
    BigDecimal chiffreAffaires
) {}