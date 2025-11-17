package com.emak.crm.mapper;

import com.emak.crm.dto.ProduitRequest;
import com.emak.crm.dto.ProduitResponse;
import com.emak.crm.entity.Produit;

public class ProduitMapper {

    public static Produit toEntity(ProduitRequest request) {
        return Produit.builder()
                .referenceSku(request.referenceSku())
                .nomProduit(request.nomProduit())
                .description(request.description())
                .categorie(request.categorie())
                .famille(request.famille())
                .prixUnitaireHt(request.prixUnitaireHt())
                .coutUnitaire(request.coutUnitaire())
                .stock(request.stock() != null ? request.stock() : 0)
                .actif(true)
                .build();
    }

    public static ProduitResponse toResponse(Produit produit) {
        return ProduitResponse.builder()
                .id(produit.getId())
                .referenceSku(produit.getReferenceSku())
                .nomProduit(produit.getNomProduit())
                .description(produit.getDescription())
                .categorie(produit.getCategorie())
                .famille(produit.getFamille())
                .prixUnitaireHt(produit.getPrixUnitaireHt())
                .coutUnitaire(produit.getCoutUnitaire())
                .stock(produit.getStock())
                .actif(produit.getActif())
                .dateCreation(produit.getDateCreation())
                .build();
    }
}