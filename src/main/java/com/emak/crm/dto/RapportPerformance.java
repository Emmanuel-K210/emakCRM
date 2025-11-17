package com.emak.crm.dto;

import java.math.BigDecimal;
import java.util.List;

public record RapportPerformance(
    String nomCommercial,
    String periode,
    BigDecimal objectifChiffreAffaires,
    BigDecimal chiffreAffairesReel,
    BigDecimal tauxObjectifAtteint,
    Integer nombreDealsConvertis,
    BigDecimal tauxConversion,
    Integer nouveauxClients,
    BigDecimal satisfactionClient,
    List<PerformanceMensuelle> performanceMensuelle,
    List<ObjectifAtteint> objectifsAtteints,
    List<Deal> dealsEnCours
) {}