package com.emak.crm.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record KPIMetrics(
    BigDecimal chiffreAffairesMensuel,
    BigDecimal objectifMensuel,
    BigDecimal tauxObjectifAtteint,
    BigDecimal tauxConversion,
    Integer nouveauxClients,
    Integer opportunitesEnCours,
    BigDecimal valeurPipeline,
    Integer activitesCeMois,
    BigDecimal croissanceCA
) {}