package com.emak.crm.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emak.crm.dto.OpportuniteRequest;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.EtapeVente;
import com.emak.crm.enums.SourceOpportunite;
import com.emak.crm.enums.StatutOpportunite;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.exception.OperationNotAllowedException;
import com.emak.crm.mapper.OpportuniteMapper;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.repository.UtilisateurRepository;
import com.emak.crm.service.OpportuniteService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class OpportuniteServiceImpl implements OpportuniteService {

    private final OpportuniteRepository opportuniteRepository;
    private final ClientRepository clientRepository;
    private final UtilisateurRepository utilisateurRepository;

    private Opportunite getById(Number id) throws EntityNotFound {
        return opportuniteRepository.findById((long) id)
                .orElseThrow(() -> EntityNotFound.of("Opportunité non trouvée avec l'ID: " + id));
    }

    private Client getClientById(Long id) throws EntityNotFound {
        return clientRepository.findById(id)
                .orElseThrow(() -> EntityNotFound.of("Client non trouvé avec l'ID: " + id));
    }

    private Utilisateur getUtilisateurById(Long id) throws EntityNotFound {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> EntityNotFound.of("Utilisateur non trouvé avec l'ID: " + id));
    }

    /**
     * CRÉATION D'UNE OPPORTUNITÉ
     * Métier : Lancer un nouveau processus de vente
     * Workflow : Validation → Assignation → Calcul probabilité → Pipeline
     * Règles : Client obligatoire, montant estimé requis
     */
    @Override
    public OpportuniteResponse createOpportunite(OpportuniteRequest request) throws EntityNotFound {
        log.info("Création d'une nouvelle opportunité pour le client: {}", request.idClient());
        
        // Validation des données obligatoires
        validateOpportuniteRequest(request);
        
        Client client = getClientById(request.idClient());
        Utilisateur utilisateur = getUtilisateurById(request.idUtilisateur());

        Opportunite opportunite = OpportuniteMapper.toEntity(request);
        opportunite.setClient(client);
        opportunite.setUtilisateur(utilisateur);
        
        // Calcul automatique de la probabilité basée sur l'étape si non fournie
        if (request.probabilite() == 0) {
            opportunite.setProbabilite(calculerProbabiliteAutomatique(EtapeVente.valueOf(request.etapeVente())));
        }
        
        // Initialisation des dates
        opportunite.setDateCreation(LocalDateTime.now());
        opportunite.setDateModification(LocalDateTime.now());
        
        Opportunite savedOpportunite = opportuniteRepository.save(opportunite);
        log.info("Opportunité créée avec ID: {}", savedOpportunite.getId());
        
        return OpportuniteMapper.toResponse(savedOpportunite);
    }

    /**
     * CONSULTATION OPPORTUNITÉ
     * Métier : Voir tous les détails d'une affaire en cours
     * Données : Historique négociation, documents, participants
     */
    @Override
    public OpportuniteResponse getOpportunite(Long id) throws EntityNotFound {
        log.debug("Consultation de l'opportunité ID: {}", id);
        Opportunite opportunite = getById(id);
        return OpportuniteMapper.toResponse(opportunite);
    }

    /**
     * LISTE OPPORTUNITÉS
     * Métier : Vue globale du pipeline commercial
     * Usage : Priorisation, répartition charge, reporting
     */
    @Override
    public List<OpportuniteResponse> getAllOpportunites() {
        log.debug("Récupération de toutes les opportunités");
        return opportuniteRepository.findAll()
                .stream()
                .map(OpportuniteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OpportuniteResponse> getAllOpportunites(Pageable pageable) {
        log.debug("Récupération des opportunités paginées");
        return opportuniteRepository.findAll(pageable)
                .map(OpportuniteMapper::toResponse);
    }

    /**
     * OPPORTUNITÉS PAR ÉTAPE
     * Métier : Filtrer les affaires par phase du processus de vente
     * Usage : Actions ciblées par étape, suivi progression
     */
    @Override
    public List<OpportuniteResponse> getOpportunitesByEtape(String etape) {
        log.debug("Récupération des opportunités par étape: {}", etape);
        try {
            EtapeVente etapeVente = EtapeVente.valueOf(etape.toUpperCase());
            return opportuniteRepository.findByEtapeVente(etapeVente)
                    .stream()
                    .map(OpportuniteMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("Étape de vente invalide: {}", etape);
            return Collections.emptyList();
        }
    }

    /**
     * OPPORTUNITÉS PAR COMMERCIAL
     * Métier : Voir le portefeuille d'affaires d'un commercial
     * Usage : Management, coaching, rééquilibrage charge
     */
    @Override
    public List<OpportuniteResponse> getOpportunitesByCommercial(Long commercialId) throws EntityNotFound {
        log.debug("Récupération des opportunités pour le commercial ID: {}", commercialId);
        Utilisateur commercial = getUtilisateurById(commercialId);
        return opportuniteRepository.findByUtilisateur(commercial)
                .stream()
                .map(OpportuniteMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * MISE À JOUR OPPORTUNITÉ
     * Métier : Modifier les informations d'une affaire en cours
     * Workflow : Vérification permissions → Historique → Recalcul probabilité
     */
    @Override
    public OpportuniteResponse updateOpportunite(Long id, OpportuniteRequest request) throws EntityNotFound {
        log.info("Mise à jour de l'opportunité ID: {}", id);
        
        Opportunite opportuniteExistante = getById(id);
        Client client = getClientById(request.idClient());
        Utilisateur utilisateur = getUtilisateurById(request.idUtilisateur());

        // Sauvegarde de l'ancienne étape pour le log
        EtapeVente ancienneEtape = opportuniteExistante.getEtapeVente();
        
        // Mise à jour des champs
        opportuniteExistante.setClient(client);
        opportuniteExistante.setUtilisateur(utilisateur);
        opportuniteExistante.setNomOpportunite(request.nomOpportunite());
        opportuniteExistante.setEtapeVente(EtapeVente.valueOf(request.etapeVente()));
        opportuniteExistante.setProbabilite(request.probabilite());
        opportuniteExistante.setStatut(StatutOpportunite.valueOf(request.statut()));
        opportuniteExistante.setMontantEstime(request.montantEstime());
        opportuniteExistante.setDateCloturePrevue(request.dateCloturePrevue());
        opportuniteExistante.setDescription(request.description());
        opportuniteExistante.setSource(SourceOpportunite.valueOf(request.source()));
        opportuniteExistante.setDateModification(LocalDateTime.now());

        Opportunite opportuniteMiseAJour = opportuniteRepository.save(opportuniteExistante);
        
        // Log de changement d'étape si applicable
        if (!ancienneEtape.equals(opportuniteExistante.getEtapeVente())) {
            log.info("Changement d'étape pour l'opportunité {}: {} → {}", 
                    id, ancienneEtape, opportuniteExistante.getEtapeVente());
        }
        
        return OpportuniteMapper.toResponse(opportuniteMiseAJour);
    }

    /**
     * SUPPRESSION OPPORTUNITÉ
     * Métier : Archiver une opportunité perdue ou abandonnée
     * Règles : Conservation historique pour analyse
     */
    @Override
    public void deleteOpportunite(Long id) throws EntityNotFound {
        log.info("Suppression de l'opportunité ID: {}", id);
        Opportunite opportunite = getById(id);
        
        // Vérification que l'opportunité peut être supprimée
        if (opportunite.getStatut() == StatutOpportunite.GAGNEE) {
            throw new OperationNotAllowedException("Impossible de supprimer une opportunité gagnée");
        }
        
        opportuniteRepository.delete(opportunite);
        log.info("Opportunité ID: {} supprimée avec succès", id);
    }

    /**
     * CHANGEMENT D'ÉTAPE
     * Métier : Faire progresser une opportunité dans le pipeline
     * Workflow : Validation transition → Notification → Mise à jour probabilité
     * Exemple : Qualification → Proposition (probabilité 40% → 60%)
     */
    @Override
    public void changerEtapeOpportunite(Long id, String nouvelleEtape) throws EntityNotFound {
        log.info("Changement d'étape pour l'opportunité ID: {} → {}", id, nouvelleEtape);
        
        Opportunite opportunite = getById(id);
        EtapeVente nouvelleEtapeEnum;
        
        try {
            nouvelleEtapeEnum = EtapeVente.valueOf(nouvelleEtape.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Étape de vente invalide: " + nouvelleEtape);
        }
        
        // Validation de la transition
        validerTransitionEtape(opportunite.getEtapeVente(), nouvelleEtapeEnum);
        
        // Mise à jour de l'étape et de la probabilité
        opportunite.setEtapeVente(nouvelleEtapeEnum);
        opportunite.setProbabilite(calculerProbabiliteAutomatique(nouvelleEtapeEnum));
        opportunite.setDateModification(LocalDateTime.now());
        
        opportuniteRepository.save(opportunite);
        log.info("Opportunité ID: {} mise à jour vers l'étape: {}", id, nouvelleEtape);
    }

    /**
     * CALCUL CHIFFRE D'AFFAIRES BRUT
     * Métier : Estimation du CA potentiel sur le pipeline actuel
     * Méthode : Somme (montant estimé × probabilité) pour toutes les opps
     */
    @Override
    public BigDecimal calculerCABrut() {
        log.debug("Calcul du chiffre d'affaires brut du pipeline");
        
        return opportuniteRepository.findAll()
                .stream()
                .filter(opp -> opp.getStatut() == StatutOpportunite.EN_COURS)
                .map(opp -> opp.getMontantEstime()
                        .multiply(BigDecimal.valueOf(opp.getProbabilite()))
                        .divide(BigDecimal.valueOf(100)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * STATISTIQUES PIPELINE DÉTAILLÉES
     * Métier : Analyse approfondie des performances du pipeline
     * Métriques : Taux conversion par étape, durée moyenne cycle
     */
    @Override
    public Map<String, Object> getStatsPipeline() {
        log.debug("Génération des statistiques détaillées du pipeline");
        
        Map<String, Object> stats = new HashMap<>();
        List<Opportunite> toutesOpportunites = opportuniteRepository.findAll();
        
        // Nombre d'opportunités par étape
        Map<EtapeVente, Long> countByEtape = toutesOpportunites.stream()
                .filter(opp -> opp.getStatut() == StatutOpportunite.EN_COURS)
                .collect(Collectors.groupingBy(Opportunite::getEtapeVente, Collectors.counting()));
        stats.put("opportunitesParEtape", countByEtape);
        
        // Chiffre d'affaires par étape
        Map<EtapeVente, BigDecimal> caByEtape = toutesOpportunites.stream()
                .filter(opp -> opp.getStatut() == StatutOpportunite.EN_COURS)
                .collect(Collectors.groupingBy(
                    Opportunite::getEtapeVente,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        opp -> opp.getMontantEstime()
                                .multiply(BigDecimal.valueOf(opp.getProbabilite()))
                                .divide(BigDecimal.valueOf(100)),
                        BigDecimal::add
                    )
                ));
        stats.put("chiffreAffairesParEtape", caByEtape);
        
        // Taux de conversion global
        long totalOpportunites = toutesOpportunites.size();
        long opportunitesGagnees = toutesOpportunites.stream()
                .filter(opp -> opp.getStatut() == StatutOpportunite.GAGNEE)
                .count();
        double tauxConversionGlobal = totalOpportunites > 0 ? 
                (double) opportunitesGagnees / totalOpportunites * 100 : 0;
        stats.put("tauxConversionGlobal", tauxConversionGlobal);
        
        // Durée moyenne du cycle de vente
        double dureeMoyenneCycle = toutesOpportunites.stream()
                .filter(opp -> opp.getStatut() == StatutOpportunite.GAGNEE && opp.getDateCloturePrevue() != null)
                .mapToLong(opp -> ChronoUnit.DAYS.between(opp.getDateCreation(), opp.getDateCloturePrevue()))
                .average()
                .orElse(0);
        stats.put("dureeMoyenneCycleJours", dureeMoyenneCycle);
        
        // Valeur moyenne des opportunités
        BigDecimal valeurMoyenne = toutesOpportunites.stream()
                .map(Opportunite::getMontantEstime)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(totalOpportunites > 0 ? BigDecimal.valueOf(totalOpportunites) : BigDecimal.ONE, 2, BigDecimal.ROUND_HALF_UP);
        stats.put("valeurMoyenneOpportunite", valeurMoyenne);
        
        // Opportunités en retard (date de clôture dépassée)
        long opportunitesEnRetard = toutesOpportunites.stream()
                .filter(opp -> opp.getStatut() == StatutOpportunite.EN_COURS)
                .filter(opp -> opp.getDateCloturePrevue() != null && opp.getDateCloturePrevue().isBefore(LocalDate.now()))
                .count();
        stats.put("opportunitesEnRetard", opportunitesEnRetard);
        
        return stats;
    }

    // Méthodes helper privées

    private void validateOpportuniteRequest(OpportuniteRequest request) {
        if (request.nomOpportunite() == null || request.nomOpportunite().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'opportunité est obligatoire");
        }
        if (request.montantEstime() == null || request.montantEstime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant estimé doit être supérieur à zéro");
        }
        if (request.dateCloturePrevue() == null || request.dateCloturePrevue().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de clôture prévue doit être dans le futur");
        }
    }

    private int calculerProbabiliteAutomatique(EtapeVente etape) {
        switch (etape) {
            case PROSPECTION:
                return 10;
            case QUALIFICATION:
                return 25;
            case PROPOSITION:
                return 50;
            case NEGOCIATION:
                return 75;
            case SIGNATURE:
                return 90;
            case GAGNE:
                return 100;
            case PERDU:
                return 0;
            default:
                return 0;
        }
    }

    private void validerTransitionEtape(EtapeVente ancienneEtape, EtapeVente nouvelleEtape) {
        // Implémentez ici la logique de validation des transitions d'étape
        // Par exemple, empêcher de revenir en arrière dans certains cas
        if (ancienneEtape.ordinal() > nouvelleEtape.ordinal() && 
            !List.of(EtapeVente.GAGNE, EtapeVente.PERDU).contains(nouvelleEtape)) {
            log.warn("Transition d'étape inhabituelle: {} → {}", ancienneEtape, nouvelleEtape);
        }
    }

    // Méthodes existantes maintenues pour la compatibilité

    @Override
    public List<OpportuniteResponse> findAll() {
        return getAllOpportunites();
    }

    @Override
    public OpportuniteResponse save(OpportuniteRequest request) {
        try {
            return createOpportunite(request);
        } catch (EntityNotFound e) {
            log.error("Erreur lors de la création de l'opportunité", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportuniteResponse saver(OpportuniteRequest request) throws EntityNotFound {
        return createOpportunite(request);
    }

    @Override
    public OpportuniteResponse findById(Number id) throws EntityNotFound {
        return getOpportunite((Long) id);
    }

    @Override
    public void deleteById(Number id) throws EntityNotFound {
        deleteOpportunite((Long) id);
    }

    @Override
    public OpportuniteResponse update(Number id, OpportuniteRequest request) throws EntityNotFound {
        return updateOpportunite((Long) id, request);
    }

    @Override
    public void changerEtapeOpportunite(Number id, String nouvelleEtape) throws EntityNotFound {
        changerEtapeOpportunite((Long) id, nouvelleEtape);
    }

    @Override
    public List<OpportuniteResponse> getOpportuniteByClientId(Long clientId) {
        log.debug("Récupération des opportunités pour le client ID: {}", clientId);
        return opportuniteRepository.findByClientId(clientId)
                .stream()
                .map(OpportuniteMapper::toResponse)
                .collect(Collectors.toList());
    }

	@Override
	public Page<OpportuniteResponse> findAll(Pageable pageable) {
		
		return opportuniteRepository.findAll(pageable).map(OpportuniteMapper::toResponse);
	}
}