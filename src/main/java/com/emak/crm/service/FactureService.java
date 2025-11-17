package com.emak.crm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.emak.crm.dto.FactureRequest;
import com.emak.crm.dto.FactureResponse;
import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.exception.EntityNotFound;

public interface FactureService{

	/**
	 * CRÉATION D'UNE FACTURE
	 * Métier : Établir un document de vente formalisé
	 * Workflow : Référencement devis → Calcul échéances → Numérotation
	 */
	FactureResponse createFacture(FactureRequest request);

	/**
	 * CONSULTATION FACTURE
	 * Métier : Voir le détail d'une facture et son état de paiement
	 * Usage : Suivi encaissement, relance, comptabilité
	 */
	FactureResponse getFacture(Long id) throws EntityNotFound ;

	/**
	 * FACTURES EN RETARD
	 * Métier : Identifier les factures non payées après échéance
	 * Usage : Relance client, gestion trésorerie
	 */
	List<FactureResponse> getFacturesEnRetard();

	/**
	 * ENREGISTRER UN PAIEMENT
	 * Métier : Marquer une facture comme partiellement ou totalement payée
	 * Workflow : Validation montant → Mise à jour solde → Historique paiement
	 */
	FactureResponse enregistrerPaiement(Long factureId, PaiementRequest request) throws EntityNotFound ;
	List<FactureResponse> getFacturesByClient(Long clientId);
	FactureResponse annulerFacture(Long factureId, String raison) throws EntityNotFound;
	Page<FactureResponse> findAll(Pageable pageable);
}
