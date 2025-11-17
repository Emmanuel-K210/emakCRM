package com.emak.crm.mapper;

import java.time.LocalDateTime;

import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.dto.PaiementResponse;
import com.emak.crm.entity.Facture;
import com.emak.crm.entity.Paiement;
import com.emak.crm.enums.ModePaiement;
import com.emak.crm.enums.StatutPaiement;

public class PaiementMapper {

    public static Paiement toEntity(PaiementRequest request, Facture facture) {
        return Paiement.builder()
                .facture(facture)
                .libelle(request.libelle())
                .datePaiement(request.datePaiement())
                .montant(request.montant())
                .modePaiement(convertToModePaiement(request.modePaiement()))
                .referenceTransaction(request.referenceTransaction())
                .statut(convertToStatutPaiement(request.statut()))
                .dateCreation(LocalDateTime.now())
                .build();
    }

    public static PaiementResponse toResponse(Paiement paiement) {
        return PaiementResponse.builder()
                .id(paiement.getId())
                .datePaiement(paiement.getDatePaiement())
                .montant(paiement.getMontant())
                .modePaiement(paiement.getModePaiement() != null ? 
                    paiement.getModePaiement().name() : null)
                .referenceTransaction(paiement.getReferenceTransaction())
                .statut(paiement.getStatut() != null ? 
                    paiement.getStatut().name() : null)
                .idFacture(paiement.getFacture() != null ? 
                    paiement.getFacture().getId() : null)
                .numeroFacture(paiement.getFacture() != null && 
                    paiement.getFacture().getNumeroFacture() != null ? 
                    paiement.getFacture().getNumeroFacture() : null)
                .dateCreation(paiement.getDateCreation())
                .build();
    }

    // Méthodes de conversion sécurisées
    private static ModePaiement convertToModePaiement(String modePaiement) {
        if (modePaiement == null) {
            return ModePaiement.VIREMENT; // Valeur par défaut
        }
        try {
            return ModePaiement.valueOf(modePaiement.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Mode de paiement invalide: " + modePaiement);
        }
    }

    private static StatutPaiement convertToStatutPaiement(String statut) {
        if (statut == null) {
            return StatutPaiement.VALIDE; // Valeur par défaut
        }
        try {
            return StatutPaiement.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut de paiement invalide: " + statut);
        }
    }
}