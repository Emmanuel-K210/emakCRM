package com.emak.crm.dto;

import java.math.BigDecimal;
import java.util.List;

public record EvolutionVentes(
    BigDecimal croissanceMensuelle,
    BigDecimal croissanceAnnuelle,
    List<PointDonnees> historique
) {}
