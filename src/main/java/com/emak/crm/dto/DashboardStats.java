package com.emak.crm.dto;

import java.math.BigDecimal;

public record DashboardStats(
    long totalClients,
    long totalVentes,
    BigDecimal montantFactures,
    long facturesNonPayees,
    long totalOpportunites
) {}