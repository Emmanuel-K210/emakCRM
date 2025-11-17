package com.emak.crm.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emak.crm.dto.ProduitRequest;
import com.emak.crm.dto.ProduitResponse;
import com.emak.crm.entity.Produit;
import com.emak.crm.enums.StatutDevis;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.ProduitMapper;
import com.emak.crm.repository.LigneDevisRepository;
import com.emak.crm.repository.ProduitRepository;
import com.emak.crm.service.ProduitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final LigneDevisRepository ligneDevisRepository;

    private Produit getById(Number id) throws EntityNotFound {
        return produitRepository.findById(id.longValue())
                .orElseThrow(() -> EntityNotFound.of("Produit non trouvé avec l'id: " + id));
    }

    /**
     * CRÉATION D'UN PRODUIT/SERVICE
     * Métier : Ajouter un nouvel article au catalogue
     * Workflow : Validation référence → Prix → Catégorisation
     */
    @Override
    public ProduitResponse save(ProduitRequest request) {
        
        // 1. Validation des données obligatoires
        if (request == null) {
            throw new IllegalArgumentException("Les données du produit ne peuvent pas être nulles");
        }
        log.info("Création d'un nouveau produit: {}", request.nomProduit());
        
        if (request.nomProduit() == null || request.nomProduit().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit est obligatoire");
        }
        
        if (request.referenceSku() == null || request.referenceSku().trim().isEmpty()) {
            throw new IllegalArgumentException("La référence SKU est obligatoire");
        }
        
        // 2. Validation de l'unicité de la référence SKU
        if (produitRepository.existsByReferenceSku(request.referenceSku())) {
            throw new IllegalArgumentException("Une référence SKU identique existe déjà: " + request.referenceSku());
        }
        
        // 3. Validation des prix
        if (request.prixUnitaireHt() == null || request.prixUnitaireHt().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix unitaire HT doit être positif ou zéro");
        }
        
        if (request.coutUnitaire() == null || request.coutUnitaire().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le coût unitaire doit être positif ou zéro");
        }
        
        // 4. Validation du stock pour les produits physiques
        if (request.stock() != null && request.stock() < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        }
        
        // 5. Conversion et sauvegarde
        Produit produit = ProduitMapper.toEntity(request);
        produit.setDateCreation(LocalDateTime.now());
        produit.setActif(true);
        
        Produit produitSauvegarde = produitRepository.save(produit);
        
        log.info("Produit créé avec succès: {} (SKU: {})", produit.getNomProduit(), produit.getReferenceSku());
        
        return ProduitMapper.toResponse(produitSauvegarde);
    }

    /**
     * CONSULTATION PRODUIT
     * Métier : Voir les détails d'un produit du catalogue
     */
    @Override
    @Transactional(readOnly = true)
    public ProduitResponse findById(Number id) throws EntityNotFound {
        log.debug("Consultation du produit ID: {}", id);
        
        Produit produit = getById(id);
        
        // Vérifier si le produit est actif
        if (!produit.getActif()) {
            log.warn("Consultation d'un produit inactif: {}", id);
        }
        
        return ProduitMapper.toResponse(produit);
    }

    /**
     * CATALOGUE COMPLET
     * Métier : Liste de tous les produits/services disponibles
     * Usage : Création devis, référence commerciale
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> findAll() {
        log.debug("Récupération de tous les produits actifs");
        
        return produitRepository.findByActifTrue().stream()
                .map(ProduitMapper::toResponse)
                .toList();
    }

    /**
     * CATALOGUE COMPLET (paginated)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProduitResponse> findAll(Pageable pageable) {
        log.debug("Récupération paginée des produits actifs");
        
        return produitRepository.findByActifTrue(pageable)
                .map(ProduitMapper::toResponse);
    }

    /**
     * PRODUITS PAR CATÉGORIE
     * Métier : Filtrer le catalogue par famille de produits
     * Usage : Navigation, propositions ciblées
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsByCategorie(String categorie) {
        log.debug("Récupération des produits par catégorie: {}", categorie);
        
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie ne peut pas être vide");
        }
        
        return produitRepository.findByCategorieAndActifTrue(categorie).stream()
                .map(ProduitMapper::toResponse)
                .toList();
    }

    /**
     * MISE À JOUR PRODUIT
     * Métier : Modifier les informations d'un produit existant
     * Règles : Conservation historique prix pour les devis existants
     */
    @Override
    public ProduitResponse update(Number id, ProduitRequest request) throws EntityNotFound {
        log.info("Mise à jour du produit ID: {}", id);
        
        Produit produit = getById(id);
        
        // 1. Validation que le produit est actif
        if (!produit.getActif()) {
            throw new IllegalStateException("Impossible de modifier un produit inactif");
        }
        
        // 2. Sauvegarde de l'ancien prix pour historique
        BigDecimal ancienPrix = produit.getPrixUnitaireHt();
        boolean prixModifie = request.prixUnitaireHt() != null && 
                             !request.prixUnitaireHt().equals(ancienPrix);
        
        // 3. Mise à jour des champs
        if (request.nomProduit() != null) {
            produit.setNomProduit(request.nomProduit());
        }
        
        if (request.referenceSku() != null && !request.referenceSku().equals(produit.getReferenceSku())) {
            // Vérifier l'unicité de la nouvelle référence SKU
            if (produitRepository.existsByReferenceSkuAndIdNot(request.referenceSku(), id.longValue())) {
                throw new IllegalArgumentException("La référence SKU existe déjà: " + request.referenceSku());
            }
            produit.setReferenceSku(request.referenceSku());
        }
        
        if (request.stock() != null) {
            produit.setStock(request.stock());
        }
        
        if (request.coutUnitaire() != null) {
            produit.setCoutUnitaire(request.coutUnitaire());
        }
        
        if (request.famille() != null) {
            produit.setFamille(request.famille());
        }
        
        if (request.prixUnitaireHt() != null) {
            produit.setPrixUnitaireHt(request.prixUnitaireHt());
        }
        
        if (request.categorie() != null) {
            produit.setCategorie(request.categorie());
        }
        
        if (request.description() != null) {
            produit.setDescription(request.description());
        }
        
        produit.setDateModification(LocalDateTime.now());
        
        Produit produitMisAJour = produitRepository.save(produit);
        
        // 4. Log de changement de prix si applicable
        if (prixModifie) {
            log.info("Changement de prix pour le produit {}: {} → {}", 
                    produit.getReferenceSku(), ancienPrix, request.prixUnitaireHt());
        }
        
        return ProduitMapper.toResponse(produitMisAJour);
    }

    /**
     * SUPPRESSION PRODUIT
     * Métier : Retirer un produit du catalogue (soft delete)
     * Règles : Impossible s'il est utilisé dans des devis en cours
     */
    @Override
    public void deleteById(Number id) throws EntityNotFound {
        log.info("Suppression du produit ID: {}", id);
        
        Produit produit = getById(id);
        
        // 1. Vérifier si le produit est utilisé dans des devis en cours
        if (estUtiliseDansDevisEnCours(produit)) {
            throw new IllegalStateException(
                "Impossible de supprimer le produit car il est utilisé dans des devis en cours. " +
                "Utilisez la désactivation à la place."
            );
        }
        
        // 2. Soft delete (désactivation)
        produit.setActif(false);
        produit.setDateModification(LocalDateTime.now());
        
        produitRepository.save(produit);
        
        log.info("Produit {} désactivé (soft delete)", produit.getReferenceSku());
    }

    /**
     * MISE À JOUR STOCK
     * Métier : Ajuster le niveau de stock d'un produit physique
     * Usage : Gestion inventaire, alertes rupture
     */
    @Override
    public void updateStock(Long produitId, Integer nouveauStock) throws EntityNotFound {
        log.info("Mise à jour du stock du produit ID: {} → {}", produitId, nouveauStock);
        
        if (nouveauStock == null || nouveauStock < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        }
        
        Produit produit = getById(produitId);
        
        // Vérifier que c'est un produit physique (avec gestion de stock)
        if (!produit.isGereStock()) {
            throw new IllegalStateException("Ce produit n'a pas de gestion de stock activée");
        }
        
        Integer ancienStock = produit.getStock();
        produit.setStock(nouveauStock);
        produit.setDateModification(LocalDateTime.now());
        
        produitRepository.save(produit);
        
        // Log du changement de stock
        log.info("Stock mis à jour pour {}: {} → {}", 
                produit.getReferenceSku(), ancienStock, nouveauStock);
        
        // Alertes automatiques (optionnel)
        gererAlertesStock(produit, ancienStock, nouveauStock);
    }

    // === MÉTHODES PRIVÉES ===

    /**
     * Vérifie si le produit est utilisé dans des devis en cours
     */
    private boolean estUtiliseDansDevisEnCours(Produit produit) {
        // Vérifier dans les lignes de devis avec statuts actifs
        long count = ligneDevisRepository.countByProduitAndDevisStatutIn(
            produit, 
            List.of(StatutDevis.BROUILLON, StatutDevis.ENVOYE, StatutDevis.EN_ATTENTE)
        );
        return count > 0;
    }

    /**
     * Gère les alertes automatiques de stock
     */
    private void gererAlertesStock(Produit produit, Integer ancienStock, Integer nouveauStock) {
        // Alerte rupture de stock
        if (ancienStock > 0 && nouveauStock == 0) {
            log.warn("🚨 RUPTURE DE STOCK - Produit: {}", produit.getReferenceSku());
        }
        
        // Alerte stock faible
        else if (ancienStock > 5 && nouveauStock <= 5) {
            log.warn("⚠️ STOCK FAIBLE - Produit: {} ({} unités)", produit.getReferenceSku(), nouveauStock);
        }
        
        // Alerte réapprovisionnement
        else if (ancienStock == 0 && nouveauStock > 0) {
            log.info("✅ STOCK RECONSTITUÉ - Produit: {} ({} unités)", produit.getReferenceSku(), nouveauStock);
        }
    }

    // === MÉTHODES SUPPLEMENTAIRES ===

    /**
     * PRODUITS EN RUPTURE DE STOCK
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsEnRupture() {
        return produitRepository.findByStockAndActifTrue(0).stream()
                .map(ProduitMapper::toResponse)
                .toList();
    }

    /**
     * PRODUITS STOCK FAIBLE
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsStockFaible() {
        return produitRepository.findByStockLessThanEqualAndActifTrue(5).stream()
                .map(ProduitMapper::toResponse)
                .toList();
    }

    /**
     * RECHERCHE PRODUITS
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> rechercherProduits(String terme) {
        if (terme == null || terme.trim().isEmpty()) {
            return List.of();
        }
        
        return produitRepository.findByNomProduitContainingIgnoreCaseAndActifTrue(terme).stream()
                .map(ProduitMapper::toResponse)
                .toList();
    }
}