package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record OpportuniteResponse(
    Long id,
    String nomOpportunite,
    String etapeVente,
    Integer probabilite,
    String statut,
    BigDecimal montantEstime,
    LocalDate dateCloturePrevue,
    String description,
    String source,
    Long idClient,
    String nomClient,
    Long idUtilisateur,
    String nomUtilisateur,
    LocalDateTime dateCreation
) {}