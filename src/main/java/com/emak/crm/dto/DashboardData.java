package com.emak.crm.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardData(
    KPIMetrics kpis,
    List<OpportuniteResponse> opportunitesRecentes,
    List<InteractionResponse> activitesRecentes,
    Map<String, Long> pipelineStats,
    Map<String, BigDecimal> performanceCommerciale,
    List<AlerteResponse> alertes,
    Map<String, BigDecimal> ventesParMois,
    Object graphiquePerformance // Pour les données de graphiques
) {}