package com.emak.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.LigneDevis;
import com.emak.crm.entity.Produit;
import com.emak.crm.enums.StatutDevis;

@Repository
public interface LigneDevisRepository extends JpaRepository<LigneDevis, Long> {
	// Vérifier si un produit est utilisé dans des devis spécifiques
    long countByProduitAndDevisStatutIn(Produit produit, List<StatutDevis> statuts);
}
