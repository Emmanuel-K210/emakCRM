package com.emak.crm.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.TacheRequest;
import com.emak.crm.dto.TacheResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Tache;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.StatutTache;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.TacheMapper;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.repository.TacheRepository;
import com.emak.crm.repository.UtilisateurRepository;
import com.emak.crm.service.TacheService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TacheServiceImpl implements TacheService{
	
	private final TacheRepository tacheRepository;
	private final UtilisateurRepository utilisateurRepository;
	private final ClientRepository clientRepository;
	private final OpportuniteRepository opportuniteRepository;
	
	private Utilisateur getUtilisateur(Long id)throws EntityNotFound{
		return utilisateurRepository.findById(id).orElseThrow(()->EntityNotFound.of("Utilisateur non trouvé"));
	}
	
	
	private Tache getTache(Long id)throws EntityNotFound{
		return tacheRepository.findById(id).orElseThrow(()->EntityNotFound.of("Tache non trouvé"));
	}
	
	
	private Client getClient(Long id)throws EntityNotFound{
		return clientRepository.findById(id).orElseThrow(()->EntityNotFound.of("Client non trouvé"));
	}
	
	private Opportunite getOpportunite(Long id)throws EntityNotFound{
		return opportuniteRepository.findById(id).orElseThrow(()->EntityNotFound.of("Opportunite non trouvé"));
	}
	
	/**
	 * CRÉER UNE TÂCHE
	 * Métier : Planifier une action à réaliser
	 * Usage : Organisation travail, suivi actions, rappels
	 */
	@Override
	public TacheResponse createTache(TacheRequest request)throws EntityNotFound {
		Utilisateur utilisateur = getUtilisateur(request.idUtilisateur());
		Client client = getClient(request.idClient());
		Opportunite opportunite = getOpportunite(request.idOpportunite());
		
		/*if(utilisateur==null  || client == null || opportunite == null) {
			throw EntityNotFound.of("Vous ne pourrez pas enregistrer cette tache verifier les informations renseigner");
		}*/
		
		Tache tache = TacheMapper.toEntity(request);
		tache.setClient(client);
		tache.setUtilisateur(utilisateur);
		tache.setOpportunite(opportunite);
		
		return TacheMapper.toResponse(tacheRepository.save(tache));
	}

	
	/**
	 * TÂCHES PAR UTILISATEUR
	 * Métier : Liste des actions assignées à une personne
	 * Usage : Planning personnel, gestion charge
	 */
	@Override
	public List<TacheResponse> getTachesByUtilisateur(Long utilisateurId) throws EntityNotFound {
		Utilisateur utilisateur = getUtilisateur(utilisateurId);
		return tacheRepository
				.findByUtilisateur(utilisateur)
				.stream()
				.map(TacheMapper::toResponse)
				.toList();
		
	}
	
	/**
	 * TÂCHES DU JOUR
	 * Métier : Actions prévues pour la journée en cours
	 * Usage : Priorisation quotidienne, focus
	 */
	@Override
	public List<TacheResponse> getTachesDuJour() {
		
		return null;
	}

	
	/**
	 * TÂCHES EN RETARD
	 * Métier : Actions non réalisées dans les délais
	 * Usage : Identification problèmes, réplanification
	 */
	@Override
	public List<TacheResponse> getTachesEnRetard() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * CHANGEMENT STATUT TÂCHE
	 * Métier : Mettre à jour l'avancement d'une action
	 * Workflow : À faire → En cours → Terminé
	 * @throws EntityNotFound 
	 */
	@Override
	public TacheResponse updateStatutTache(Long id, String nouveauStatut) throws EntityNotFound {
		Tache tache = getTache(id);
		tache.setStatut(StatutTache.valueOf(nouveauStatut));
		 
	
		return TacheMapper.toResponse(tache);
	}
	
    /**
     * MISE À JOUR COMPLÈTE D'UNE TÂCHE
     */
    @Override
    public TacheResponse updateTache(Long id, TacheRequest request) throws EntityNotFound {
        Tache tacheExistante = getTache(id);
        Utilisateur utilisateur = getUtilisateur(request.idUtilisateur());
        Client client = getClient(request.idClient());
        Opportunite opportunite = getOpportunite(request.idOpportunite());
        
        Tache tacheMiseAJour = TacheMapper.toEntity(request);
        tacheMiseAJour.setId(id);
        tacheMiseAJour.setUtilisateur(utilisateur);
        tacheMiseAJour.setClient(client);
        tacheMiseAJour.setOpportunite(opportunite);
        
        return TacheMapper.toResponse(tacheRepository.save(tacheMiseAJour));
    }

    /**
     * SUPPRIMER UNE TÂCHE
     */
    @Override
    public void deleteTache(Long id) throws EntityNotFound {
        Tache tache = getTache(id);
        tacheRepository.delete(tache);
    }

    /**
     * RÉCUPÉRER UNE TÂCHE PAR SON ID
     */
    @Override
    public TacheResponse getTacheById(Long id) throws EntityNotFound {
        Tache tache = getTache(id);
        return TacheMapper.toResponse(tache);
    }

    /**
     * TÂCHES PAR OPPORTUNITÉ
     */
    @Override
    public List<TacheResponse> getTachesByOpportunite(Long opportuniteId) throws EntityNotFound {
        Opportunite opportunite = getOpportunite(opportuniteId);
        return tacheRepository
                .findByOpportunite(opportunite)
                .stream()
                .map(TacheMapper::toResponse)
                .toList();
    }

    /**
     * TÂCHES PAR STATUT
     */
    @Override
    public List<TacheResponse> getTachesByStatut(String statut) {
        StatutTache statutTache = StatutTache.valueOf(statut);
        return tacheRepository
                .findByStatut(statutTache)
                .stream()
                .map(TacheMapper::toResponse)
                .toList();
    }

}
