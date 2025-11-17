package com.emak.crm.dto;

import java.math.BigDecimal;

public record Deal(
    String nom,
    BigDecimal montant,
    String statut,
    String client
) {}