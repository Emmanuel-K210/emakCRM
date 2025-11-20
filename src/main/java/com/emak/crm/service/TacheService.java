package com.emak.crm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.TacheRequest;
import com.emak.crm.dto.TacheResponse;
import com.emak.crm.exception.EntityNotFound;
@Service
public interface TacheService {

	/**
	 * CRÉER UNE TÂCHE
	 * Métier : Planifier une action à réaliser
	 * Usage : Organisation travail, suivi actions, rappels
	 */
	TacheResponse createTache(TacheRequest request)throws EntityNotFound;

	
	/**
	 * TÂCHES PAR UTILISATEUR
	 * Métier : Liste des actions assignées à une personne
	 * Usage : Planning personnel, gestion charge
	 */
	List<TacheResponse> getTachesByUtilisateur(Long utilisateurId)throws EntityNotFound ;
	
	/**
	 * TÂCHES DU JOUR
	 * Métier : Actions prévues pour la journée en cours
	 * Usage : Priorisation quotidienne, focus
	 */
	List<TacheResponse> getTachesDuJour();
	
	/**
	 * TÂCHES EN RETARD
	 * Métier : Actions non réalisées dans les délais
	 * Usage : Identification problèmes, réplanification
	 */
	List<TacheResponse> getTachesEnRetard();

	
	/**
	 * CHANGEMENT STATUT TÂCHE
	 * Métier : Mettre à jour l'avancement d'une action
	 * Workflow : À faire → En cours → Terminé
	 */
	TacheResponse updateStatutTache(Long id, String nouveauStatut) throws EntityNotFound ;

    /**
     * TÂCHES PAR STATUT
     */
	 List<TacheResponse> getTachesByStatut(String statut);
	 /**
	     * TÂCHES PAR OPPORTUNITÉ
	     */
	 List<TacheResponse> getTachesByOpportunite(Long opportuniteId) throws EntityNotFound;
	 
	 
	 /**
	     * RÉCUPÉRER UNE TÂCHE PAR SON ID
	     */
	    TacheResponse getTacheById(Long id) throws EntityNotFound;
	    
	    /**
	     * SUPPRIMER UNE TÂCHE
	     */
	   void deleteTache(Long id) throws EntityNotFound;
	   
	   /**
	     * MISE À JOUR COMPLÈTE D'UNE TÂCHE
	     */
	   TacheResponse updateTache(Long id, TacheRequest request) throws EntityNotFound;


	   List<TacheResponse> getAllTaches();


	   List<TacheResponse> getTachesByClient(Long clientId) throws EntityNotFound;
}
