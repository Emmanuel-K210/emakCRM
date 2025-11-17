package com.emak.crm.dto;

import java.util.List;

public record ExportRequest(
    ClientSearchRequest searchCriteria,
    String format, // EXCEL, PDF, CSV
    List<String> colonnes, // Champs à exporter
    boolean inclureEntete,
    String nomFichier
) {
    
    public ExportRequest {
        if (format == null) format = "EXCEL";
        if (colonnes == null) colonnes = List.of("nom", "prenom", "entreprise", "email", "telephone", "ville");
        if (nomFichier == null) nomFichier = "clients_export";
    }
}