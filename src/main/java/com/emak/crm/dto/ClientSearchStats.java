package com.emak.crm.dto;

import java.util.Map;

import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;

public record ClientSearchStats(
    long totalClients,
    long prospectsCount,
    long clientsCount,
    long anciensClientsCount,
    double scoreMoyen,
    Map<TypeClient, Long> countByType,
    Map<StatutClient, Long> countByStatut,
    Map<String, Long> countByVille,
    Map<OrigineClient, Long> countByOrigine,
    long clientsActifs,
    long clientsInactifs
) {
    
    public ClientSearchStats {
        if (countByType == null) countByType = Map.of();
        if (countByStatut == null) countByStatut = Map.of();
        if (countByVille == null) countByVille = Map.of();
        if (countByOrigine == null) countByOrigine = Map.of();
    }
}