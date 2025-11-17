package com.emak.crm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Produit;


@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
	// Produits actifs uniquement
    List<Produit> findByActifTrue();
    Page<Produit> findByActifTrue(Pageable pageable);
    
    // Recherche par catégorie
    List<Produit> findByCategorieAndActifTrue(String categorie);
    
    // Vérification unicité SKU
    boolean existsByReferenceSku(String referenceSku);
    boolean existsByReferenceSkuAndIdNot(String referenceSku, Long id);
    
    // Gestion stock
    List<Produit> findByStockAndActifTrue(Integer stock);
    List<Produit> findByStockLessThanEqualAndActifTrue(Integer stock);
    
    // Recherche
    List<Produit> findByNomProduitContainingIgnoreCaseAndActifTrue(String terme);
  
    
}
