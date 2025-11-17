package com.emak.crm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.ProduitRequest;
import com.emak.crm.dto.ProduitResponse;
import com.emak.crm.exception.EntityNotFound;
@Service
public interface ProduitService extends CrudService<ProduitRequest, ProduitResponse> {

	void updateStock(Long produitId, Integer nouveauStock) throws EntityNotFound;

	/**
	 * PRODUITS PAR CATÉGORIE Métier : Filtrer le catalogue par famille de produits
	 * Usage : Navigation, propositions ciblées
	 */
	List<ProduitResponse> getProduitsByCategorie(String categorie);

	List<ProduitResponse> getProduitsEnRupture();
	
	 List<ProduitResponse> rechercherProduits(String terme);
	 
	  List<ProduitResponse> getProduitsStockFaible();
}
