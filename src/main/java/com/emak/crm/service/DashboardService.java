package com.emak.crm.service;

import com.emak.crm.dto.DashboardData;
import com.emak.crm.dto.InteractionResponse;
import com.emak.crm.dto.OpportuniteResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
@Service
public interface DashboardService {
    
    DashboardData getDashboardData();
    Map<String, Object> getMetrics();
    List<OpportuniteResponse> getOpportunitesRecentes();
    List<InteractionResponse> getActivitesRecentes();
    Map<String, Long> getPipelineStats();
    
    // Méthodes existantes pour compatibilité
    long totalClients();
    BigDecimal montantFactures();
    long facturesNonPayees();
    long totalOpportunites();
    Map<String, Long> opportunitesParEtape();
    long totalVentes();
    Map<String, BigDecimal> ventesParMois();
}