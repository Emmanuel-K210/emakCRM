package com.emak.crm.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.UtilisateurRequest;
import com.emak.crm.dto.UtilisateurResponse;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.Roles;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.UtilisateurMapper;
import com.emak.crm.repository.UtilisateurRepository;
import com.emak.crm.service.UtilisateurService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    private Utilisateur getById(Number id) throws EntityNotFound {
        return utilisateurRepository.findById((long) id)
                .orElseThrow(() -> EntityNotFound.of("L'utilisateur"));
    }
    
    /**
     * LISTE DES UTILISATEURS
     * Métier : Vue d'ensemble de tous les comptes pour l'administration
     * Usage : Gestion des accès, statistiques équipe
     */
    @Override
    public List<UtilisateurResponse> findAll() {
        return utilisateurRepository.findAll().stream()
                .map(UtilisateurMapper::toResponse)
                .toList();
    }
    
    
    /**
     * CRÉATION D'UN UTILISATEUR
     * Métier : Permet à l'admin de créer un nouveau compte utilisateur
     * Workflow : Validation données → Hash mot de passe → Assignation rôle → Sauvegarde
     * Règles : Email unique, rôle obligatoire, mot de passe fort
     */
    
    @Override
    public UtilisateurResponse save(UtilisateurRequest request) {
        return UtilisateurMapper.toResponse(utilisateurRepository.save(UtilisateurMapper.toEntity(request)));
    }
    
    
    /**
     * RÉCUPÉRATION UTILISATEUR
     * Métier : Obtenir les infos détaillées d'un utilisateur pour affichage/édition
     * Usage : Fiche utilisateur, profil, modification
     */
    @Override
    public UtilisateurResponse findById(Number id) throws EntityNotFound {
        return UtilisateurMapper.toResponse(getById(id));
    }
    
    /**
     * SUPPRESSION UTILISATEUR
     * Métier : Désactiver un compte utilisateur (soft delete)
     * Règles : Empêcher suppression si l'utilisateur a des clients assignés
     */
    @Override
    public void deleteById(Number id) throws EntityNotFound {
        Utilisateur utilisateur = getById(id);
        utilisateurRepository.delete(utilisateur);
    }
    
    /**
     * MISE À JOUR UTILISATEUR
     * Métier : Modifier les informations d'un utilisateur existant
     * Workflow : Vérification existence → Validation modifications → Sauvegarde
     */
    @Override
    public UtilisateurResponse update(Number id, UtilisateurRequest request) throws EntityNotFound {
        Utilisateur utilisateur = getById(id);
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());
        utilisateur.setMotPasse(request.motPasse());
        utilisateur.setRole(Roles.of(request.role()));
        utilisateur.setEquipe(request.equipe());
        utilisateur.setDateEmbauche(LocalDate.parse(request.dateEmbauche()));
        return UtilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }
    
    
    /**
     * LISTE DES UTILISATEURS
     * Métier : Vue d'ensemble de tous les comptes pour l'administration
     * Usage : Gestion des accès, statistiques équipe
     * 
     */
	@Override
	public Page<UtilisateurResponse> findAll(Pageable pageable) {
		Page<Utilisateur> page = utilisateurRepository.findAll(pageable);
		return page.map(UtilisateurMapper::toResponse);
	}
}
