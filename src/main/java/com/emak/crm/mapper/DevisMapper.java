package com.emak.crm.mapper;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.emak.crm.dto.DevisRequest;
import com.emak.crm.dto.DevisResponse;
import com.emak.crm.dto.LigneDevisRequest;
import com.emak.crm.dto.LigneDevisResponse;
import com.emak.crm.entity.Devis;
import com.emak.crm.entity.LigneDevis;
import com.emak.crm.enums.StatutDevis;

public class DevisMapper {

    public static Devis toEntity(DevisRequest request) {
        return Devis.builder()
                .numeroDevis(request.numeroDevis())
                .dateEmission(request.dateEmission())
                .dateValidite(request.dateValidite())
                .montantHt(request.montantHt())
                .montantTtc(request.montantTtc())
                .tauxTva(request.tauxTva() != null ? request.tauxTva() :new BigDecimal("20.00"))
                .statut(StatutDevis.valueOf(request.statut()))
                .build();
    }

    public static LigneDevis toLigneEntity(LigneDevisRequest request) {
        return LigneDevis.builder()
                .quantite(request.quantite())
                .prixUnitaireHt(request.prixUnitaireHt())
                .tauxTva(request.tauxTva() != null ? request.tauxTva() : new BigDecimal("20.00"))
                .remise(request.remise() != null ? request.remise() : BigDecimal.ZERO)
                .build();
    }

    public static DevisResponse toResponse(Devis devis) {
        return DevisResponse.builder()
                .idDevis(devis.getId())
                .numeroDevis(devis.getNumeroDevis())
                .dateEmission(devis.getDateEmission())
                .dateValidite(devis.getDateValidite())
                .montantHt(devis.getMontantHt())
                .montantTtc(devis.getMontantTtc())
                .tauxTva(devis.getTauxTva())
                .statut(devis.getStatut().name())
                .idOpportunite(devis.getOpportunite().getId())
                .nomOpportunite(devis.getOpportunite().getNomOpportunite())
                .idUtilisateur(devis.getUtilisateur().getId())
                .nomUtilisateur(devis.getUtilisateur().getPrenom() + " " + devis.getUtilisateur().getNom())
                .lignes(devis.getLignes().stream()
                    .map(DevisMapper::toLigneResponse)
                    .collect(Collectors.toList()))
                .dateCreation(devis.getDateCreation())
                .build();
    }

    public static LigneDevisResponse toLigneResponse(LigneDevis ligne) {
        return LigneDevisResponse.builder()
                .id(ligne.getId())
                .quantite(ligne.getQuantite())
                .prixUnitaireHt(ligne.getPrixUnitaireHt())
                .tauxTva(ligne.getTauxTva())
                .remise(ligne.getRemise())
                .idProduit(ligne.getProduit().getId())
                .nomProduit(ligne.getProduit().getNomProduit())
                .referenceSku(ligne.getProduit().getReferenceSku())
                .build();
    }
}