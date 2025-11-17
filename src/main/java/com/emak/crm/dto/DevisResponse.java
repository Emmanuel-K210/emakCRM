package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;



@Builder
public record DevisResponse(
    Long idDevis,
    String numeroDevis,
    LocalDate dateEmission,
    LocalDate dateValidite,
    BigDecimal montantHt,
    BigDecimal montantTtc,
    BigDecimal tauxTva,
    String statut,
    Long idOpportunite,
    String nomOpportunite,
    Long idUtilisateur,
    String nomUtilisateur,
    List<LigneDevisResponse> lignes,
    LocalDateTime dateCreation
) {}