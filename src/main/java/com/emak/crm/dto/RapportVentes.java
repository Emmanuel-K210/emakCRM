// RapportVentes.java
package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RapportVentes(
    LocalDate dateDebut,
    LocalDate dateFin,
    BigDecimal chiffreAffairesTotal,
    BigDecimal objectifAtteint,
    List<VentesParCommercial> ventesParCommercial,
    List<VentesParProduit> ventesParProduit,
    EvolutionVentes evolution,
    List<ComparaisonMensuelle> comparaisonMensuelle
) {}





