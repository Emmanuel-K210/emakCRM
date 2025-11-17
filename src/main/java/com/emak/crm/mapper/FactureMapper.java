package com.emak.crm.mapper;

import com.emak.crm.dto.FactureRequest;
import com.emak.crm.dto.FactureResponse;
import com.emak.crm.entity.Facture;
import com.emak.crm.enums.StatutFacture;

public class FactureMapper {

    public static Facture toEntity(FactureRequest request) {
        return Facture.builder()
                .numeroFacture(request.numeroFacture())
                .dateFacture(request.dateFacture())
                .dateEcheance(request.dateEcheance())
                .montantHt(request.montantHt())
                .montantTtc(request.montantTtc())
                .tauxTva(request.tauxTva() != null ? request.tauxTva() : new java.math.BigDecimal("20.00"))
                .montantRestant(request.montantRestant() != null ? request.montantRestant() : request.montantTtc())
                .statut(StatutFacture.valueOf(request.statut()))
                .build();
    }

    public static FactureResponse toResponse(Facture facture) {
        return FactureResponse.builder()
                .idFacture(facture.getId())
                .numeroFacture(facture.getNumeroFacture())
                .dateFacture(facture.getDateFacture())
                .dateEcheance(facture.getDateEcheance())
                .montantHt(facture.getMontantHt())
                .montantTtc(facture.getMontantTtc())
                .tauxTva(facture.getTauxTva())
                .montantRestant(facture.getMontantRestant())
                .statut(facture.getStatut().name())
                .idDevis(facture.getDevis() != null ? facture.getDevis().getId() : null)
                .numeroDevis(facture.getDevis() != null ? facture.getDevis().getNumeroDevis() : null)
                .idClient(facture.getClient().getId())
                .nomClient(facture.getClient().getEntreprise() != null ? 
                    facture.getClient().getEntreprise() : 
                    facture.getClient().getPrenom() + " " + facture.getClient().getNom())
                .idUtilisateur(facture.getUtilisateur().getId())
                .nomUtilisateur(facture.getUtilisateur().getPrenom() + " " + facture.getUtilisateur().getNom())
                .paiements(facture.getPaiements().stream()
                    .map(PaiementMapper::toResponse)
                    .collect(java.util.stream.Collectors.toList()))
                .dateCreation(facture.getDateCreation())
                .build();
    }
}