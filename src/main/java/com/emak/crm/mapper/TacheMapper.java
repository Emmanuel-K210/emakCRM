package com.emak.crm.mapper;

import com.emak.crm.dto.TacheRequest;
import com.emak.crm.dto.TacheResponse;
import com.emak.crm.entity.Tache;
import com.emak.crm.enums.PrioriteTache;
import com.emak.crm.enums.StatutTache;

public class TacheMapper {

    public static Tache toEntity(TacheRequest request) {
        return Tache.builder()
                .titre(request.titre())
                .description(request.description())
                .dateDebut(request.dateDebut())
                .dateEcheance(request.dateEcheance())
                .priorite(PrioriteTache.valueOf(request.priorite()))
                .statut(StatutTache.valueOf(request.statut()))
                .build();
    }

    public static TacheResponse toResponse(Tache tache) {
        return TacheResponse.builder()
                .idTache(tache.getId())
                .titre(tache.getTitre())
                .description(tache.getDescription())
                .dateDebut(tache.getDateDebut())
                .dateEcheance(tache.getDateEcheance())
                .priorite(tache.getPriorite().name())
                .statut(tache.getStatut().name())
                .idUtilisateur(tache.getUtilisateur().getId())
                .nomUtilisateur(tache.getUtilisateur().getPrenom() + " " + tache.getUtilisateur().getNom())
                .idClient(tache.getClient() != null ? tache.getClient().getId() : null)
                .nomClient(tache.getClient() != null ? 
                    (tache.getClient().getEntreprise() != null ? 
                        tache.getClient().getEntreprise() : 
                        tache.getClient().getPrenom() + " " + tache.getClient().getNom()) : null)
                .idOpportunite(tache.getOpportunite() != null ? tache.getOpportunite().getId() : null)
                .nomOpportunite(tache.getOpportunite() != null ? tache.getOpportunite().getNomOpportunite() : null)
                .dateCreation(tache.getDateCreation())
                .build();
    }
}