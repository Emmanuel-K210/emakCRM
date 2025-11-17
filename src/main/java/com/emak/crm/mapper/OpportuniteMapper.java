package com.emak.crm.mapper;

import com.emak.crm.dto.OpportuniteRequest;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.enums.EtapeVente;
import com.emak.crm.enums.SourceOpportunite;
import com.emak.crm.enums.StatutOpportunite;

public class OpportuniteMapper {

    public static Opportunite toEntity(OpportuniteRequest request) {
        return Opportunite.builder()
                .nomOpportunite(request.nomOpportunite())
                .etapeVente(EtapeVente.valueOf(request.etapeVente()))
                .probabilite(request.probabilite())
                .statut(StatutOpportunite.valueOf(request.statut()))
                .montantEstime(request.montantEstime())
                .dateCloturePrevue(request.dateCloturePrevue())
                .description(request.description())
                .source(request.source() != null ? 
                    SourceOpportunite.valueOf(request.source()) : null)
                .build();
    }

    public static OpportuniteResponse toResponse(Opportunite opportunite) {
        return OpportuniteResponse.builder()
                .id(opportunite.getId())
                .nomOpportunite(opportunite.getNomOpportunite())
                .etapeVente(opportunite.getEtapeVente().name())
                .probabilite(opportunite.getProbabilite())
                .statut(opportunite.getStatut().name())
                .montantEstime(opportunite.getMontantEstime())
                .dateCloturePrevue(opportunite.getDateCloturePrevue())
                .description(opportunite.getDescription())
                .source(opportunite.getSource() != null ? opportunite.getSource().name() : null)
                .idClient(opportunite.getClient().getId())
                .nomClient(opportunite.getClient().getEntreprise() != null ? 
                    opportunite.getClient().getEntreprise() : 
                    opportunite.getClient().getPrenom() + " " + opportunite.getClient().getNom())
                .idUtilisateur(opportunite.getUtilisateur().getId())
                .nomUtilisateur(opportunite.getUtilisateur().getPrenom() + " " + 
                    opportunite.getUtilisateur().getNom())
                .dateCreation(opportunite.getDateCreation())
                .build();
    }
}