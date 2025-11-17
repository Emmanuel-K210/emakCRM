package com.emak.crm.mapper;

import com.emak.crm.dto.InteractionRequest;
import com.emak.crm.dto.InteractionResponse;
import com.emak.crm.entity.Interaction;
import com.emak.crm.enums.ResultatInteraction;
import com.emak.crm.enums.TypeInteraction;

public class InteractionMapper {

    public static Interaction toEntity(InteractionRequest request) {
        return Interaction.builder()
                .type(TypeInteraction.valueOf(request.type()))
                .objet(request.objet())
                .compteRendu(request.compteRendu())
                .resultat(request.resultat() != null ? 
                    ResultatInteraction.valueOf(request.resultat()) : null)
                .dateInteraction(request.dateInteraction())
                .duree(request.duree())
                .build();
    }

    public static InteractionResponse toResponse(Interaction interaction) {
        return InteractionResponse.builder()
                .idInteraction(interaction.getId())
                .type(interaction.getType().name())
                .objet(interaction.getObjet())
                .compteRendu(interaction.getCompteRendu())
                .resultat(interaction.getResultat() != null ? interaction.getResultat().name() : null)
                .dateInteraction(interaction.getDateInteraction())
                .duree(interaction.getDuree())
                .idClient(interaction.getClient().getId())
                .nomClient(interaction.getClient().getEntreprise() != null ? 
                    interaction.getClient().getEntreprise() : 
                    interaction.getClient().getPrenom() + " " + interaction.getClient().getNom())
                .idUtilisateur(interaction.getUtilisateur().getId())
                .nomUtilisateur(interaction.getUtilisateur().getPrenom() + " " + 
                    interaction.getUtilisateur().getNom())
                .idOpportunite(interaction.getOpportunite() != null ? 
                    interaction.getOpportunite().getId() : null)
                .nomOpportunite(interaction.getOpportunite() != null ? 
                    interaction.getOpportunite().getNomOpportunite() : null)
                .dateCreation(interaction.getDateCreation())
                .build();
    }
}