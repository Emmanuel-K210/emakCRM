package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record FactureResponse(
    Long idFacture,
    String numeroFacture,
    LocalDate dateFacture,
    LocalDate dateEcheance,
    BigDecimal montantHt,
    BigDecimal montantTtc,
    BigDecimal tauxTva,
    BigDecimal montantRestant,
    String statut,
    Long idDevis,
    String numeroDevis,
    Long idClient,
    String nomClient,
    Long idUtilisateur,
    String nomUtilisateur,
    List<PaiementResponse> paiements,
    LocalDateTime dateCreation
) {}