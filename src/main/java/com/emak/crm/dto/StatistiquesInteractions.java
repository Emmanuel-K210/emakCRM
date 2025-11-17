package com.emak.crm.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatistiquesInteractions {
    private final long totalInteractions;
    private final long interactions30Jours;
    private final long actionsEnAttente;
}