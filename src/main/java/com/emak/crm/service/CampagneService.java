package com.emak.crm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.CampagneRequest;
import com.emak.crm.dto.CampagneResponse;
@Service
public interface CampagneService extends CrudService<CampagneRequest, CampagneResponse> {

	/**
	 * CAMPAGNES ACTIVES
	 * Métier : Campagnes en cours de diffusion ou planifiées
	 * Usage : Suivi exécution, coordination
	 */
	List<CampagneResponse> getCampagnesActives();

	/**
	 * EXÉCUTER UNE CAMPAGNE
	 * Métier : Lancer la diffusion d'une campagne planifiée
	 * Workflow : Sélection cibles → Préparation contenu → Diffusion → Tracking
	 */
	void executerCampagne(Long campagneId);
}
