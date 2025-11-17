package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record PaiementResponse(
        Long id,
        LocalDate datePaiement,
        BigDecimal montant,
        String modePaiement,
        String referenceTransaction,
        String statut,
        Long idFacture,
        String numeroFacture,
        LocalDateTime dateCreation
) {}
