package com.emak.crm.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emak.crm.dto.FactureRequest;
import com.emak.crm.dto.FactureResponse;
import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.entity.Devis;
import com.emak.crm.entity.Facture;
import com.emak.crm.entity.Paiement;
import com.emak.crm.enums.ModePaiement;
import com.emak.crm.enums.StatutDevis;
import com.emak.crm.enums.StatutFacture;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.FactureMapper;
import com.emak.crm.repository.DevisRepository;
import com.emak.crm.repository.FactureRepository;
import com.emak.crm.repository.PaiementRepository;
import com.emak.crm.service.FactureService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final DevisRepository devisRepository;
    private final PaiementRepository paiementRepository;

    /**
     * CRÉATION D'UNE FACTURE
     * Métier : Établir un document de vente formalisé
     * Workflow : Référencement devis → Calcul échéances → Numérotation
     */
    @Override
    public FactureResponse createFacture(FactureRequest request) {
        log.info("Création d'une nouvelle facture");
        
        // 1. Validation du devis référencé
        Devis devis = null;
        if (request.idDevis() != null) {
            devis = devisRepository.findById(request.idDevis())
                    .orElseThrow(() -> new IllegalArgumentException("Devis référencé non trouvé"));
            
            // Validation : vérifier que le devis est accepté
            if (devis.getStatut() != StatutDevis.ACCEPTE) {
                throw new IllegalStateException("Seuls les devis acceptés peuvent être facturés");
            }
        }
        
        // 2. Validation des montants
        if (request.montantHt() == null || request.montantHt().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant HT doit être positif");
        }
        
        // 3. Conversion en entité
        Facture facture = FactureMapper.toEntity(request);
        
        // 4. Génération du numéro de facture
        String numeroFacture = genererNumeroFacture();
        facture.setNumeroFacture(numeroFacture);
        
        // 5. Calcul automatique des dates si non fournies
        if (facture.getDateFacture() == null) {
            facture.setDateFacture(LocalDate.now());
        }
        
        if (facture.getDateEcheance() == null) {
            facture.setDateEcheance(facture.getDateEcheance().plusDays(30)); // 30 jours par défaut
        }
        
        // 6. Calcul automatique des totaux si non fournis
        if (facture.getMontantTtc() == null) {
            BigDecimal tauxTva = facture.getTauxTva() != null ? facture.getTauxTva() : new BigDecimal("20.00");
            BigDecimal montantTva = request.montantHt().multiply(tauxTva).divide(new BigDecimal("100"));
            facture.setMontantTva(montantTva);
            facture.setMontantTtc(request.montantHt().add(montantTva));
        }
        
        // 7. Initialisation du statut et du solde
        facture.setStatut(StatutFacture.EMISE);
        facture.setMontantRestant(facture.getMontantTtc());
        
        // 8. Association avec le devis
        facture.setDevis(devis);
        
        // 9. Sauvegarde
        Facture factureSauvegardee = factureRepository.save(facture);
        
        // 10. Mise à jour du devis si associé
        if (devis != null) {
            devis.setDejaFacture(true);
            devisRepository.save(devis);
        }
        
        log.info("Facture {} créée avec succès", numeroFacture);
        return FactureMapper.toResponse(factureSauvegardee);
    }

    /**
     * CONSULTATION FACTURE
     * Métier : Voir le détail d'une facture et son état de paiement
     * Usage : Suivi encaissement, relance, comptabilité
     * @throws EntityNotFound 
     */
    @Override
    @Transactional(readOnly = true)
    public FactureResponse getFacture(Long id) throws EntityNotFound {
        log.debug("Consultation de la facture ID: {}", id);
        
        Facture facture = factureRepository.findByIdWithDetails(id)
                .orElseThrow(() -> EntityNotFound.of("Facture non trouvée avec l'id: " + id));
        
        // Enrichir avec les données de paiement
        enrichirAvecDonneesPaiement(facture);
        
        return FactureMapper.toResponse(facture);
    }

    /**
     * FACTURES EN RETARD
     * Métier : Identifier les factures non payées après échéance
     * Usage : Relance client, gestion trésorerie
     */
    @Override
    @Transactional(readOnly = true)
    public List<FactureResponse> getFacturesEnRetard() {
        log.debug("Récupération des factures en retard");
        
        LocalDate aujourdhui = LocalDate.now();
        
        List<Facture> facturesEnRetard = factureRepository.findByStatutAndDateEcheanceBefore(
            StatutFacture.EMISE, aujourdhui
        );
        
        // Marquer les factures comme en retard (optionnel)
        facturesEnRetard.forEach(facture -> {
            if (facture.getStatut() != StatutFacture.EN_RETARD) {
                facture.setStatut(StatutFacture.EN_RETARD);
                factureRepository.save(facture);
            }
        });
        
        return facturesEnRetard.stream()
                .map(FactureMapper::toResponse)
                .toList();
    }

    /**
     * ENREGISTRER UN PAIEMENT
     * Métier : Marquer une facture comme partiellement ou totalement payée
     * Workflow : Validation montant → Mise à jour solde → Historique paiement
     * @throws EntityNotFound 
     */
    @Override
    public FactureResponse enregistrerPaiement(Long factureId, PaiementRequest request) throws EntityNotFound {
        log.info("Enregistrement d'un paiement pour la facture ID: {}", factureId);
        
        // 1. Récupération de la facture
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> EntityNotFound.of("Facture non trouvée"));
        
        // 2. Validation du paiement
        validerPaiement(facture, request);
        
        // 3. Création de l'entité paiement
        Paiement paiement = creerPaiement(facture, request);
        
        // 4. Sauvegarde du paiement
        Paiement paiementSauvegarde = paiementRepository.save(paiement);
        
        // 5. Mise à jour du solde de la facture
        mettreAJourSoldeFacture(facture, request.montant());
        
        // 6. Mise à jour du statut de la facture
        mettreAJourStatutFacture(facture);
        
        Facture factureMisAJour = factureRepository.save(facture);
        
        log.info("Paiement de {} € enregistré pour la facture {}", request.montant(), facture.getNumeroFacture());
        
        return FactureMapper.toResponse(factureMisAJour);
    }

    // === MÉTHODES PRIVÉES ===

    private String genererNumeroFacture() {
        String prefixe = "FACT";
        int annee = LocalDate.now().getYear();
        Long sequence = factureRepository.countByDateFacturationYear(annee) + 1;
        return String.format("%s-%d-%04d", prefixe, annee, sequence);
    }

    private void enrichirAvecDonneesPaiement(Facture facture) {
        // Calculer le montant payé
        BigDecimal montantPaye = paiementRepository.findMontantTotalPayeByFacture(facture.getId());
        facture.setMontantTtc(montantPaye != null ? montantPaye : BigDecimal.ZERO);
        
        // Calculer le montant restant
        BigDecimal montantRestant = facture.getMontantTtc().subtract(
            facture.getMontantTtc() != null ? facture.getMontantTtc() : BigDecimal.ZERO
        );
        facture.setMontantRestant(montantRestant);
        
        // Vérifier si la facture est en retard
        if (facture.getDateEcheance().isBefore(LocalDate.now()) && 
            facture.getStatut() == StatutFacture.EMISE) {
            facture.setStatut(StatutFacture.EN_RETARD);
        }
    }

    private void validerPaiement(Facture facture, PaiementRequest request) {
        // Validation du montant
        if (request.montant() == null || request.montant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du paiement doit être positif");
        }
        
        // Vérifier que la facture n'est pas déjà payée
        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new IllegalStateException("Cette facture est déjà entièrement payée");
        }
        
        // Vérifier que la facture n'est pas annulée
        if (facture.getStatut() == StatutFacture.ANNULEE) {
            throw new IllegalStateException("Impossible d'enregistrer un paiement sur une facture annulée");
        }
        
        // Calculer le montant restant
        BigDecimal montantRestant = facture.getMontantRestant() != null ? 
            facture.getMontantRestant() : facture.getMontantTtc();
        
        // Vérifier que le paiement ne dépasse pas le montant restant
        if (request.montant().compareTo(montantRestant) > 0) {
            throw new IllegalArgumentException(
                String.format("Le paiement (%.2f €) dépasse le montant restant (%.2f €)", 
                    request.montant(), montantRestant)
            );
        }
    }

    private Paiement creerPaiement(Facture facture, PaiementRequest request) {
        return Paiement.builder()
                .facture(facture)
                .montant(request.montant())
                .datePaiement(request.datePaiement() != null ? request.datePaiement() : LocalDate.now())
                .modePaiement(ModePaiement.valueOf(request.modePaiement()))
                .referenceTransaction(request.referenceTransaction())
                .libelle(request.libelle())
                .dateCreation(LocalDateTime.now())
                .build();
    }

    private void mettreAJourSoldeFacture(Facture facture, BigDecimal montantPaiement) {
        BigDecimal ancienMontantPaye = facture.getMontantTtc() != null ? 
            facture.getMontantTtc() : BigDecimal.ZERO;
        
        BigDecimal nouveauMontantPaye = ancienMontantPaye.add(montantPaiement);
        facture.setMontantTtc(nouveauMontantPaye);
        
        BigDecimal nouveauMontantRestant = facture.getMontantTtc().subtract(nouveauMontantPaye);
        facture.setMontantRestant(nouveauMontantRestant);
        
        facture.setDateModification(LocalDateTime.now());
    }

    private void mettreAJourStatutFacture(Facture facture) {
        if (facture.getMontantRestant().compareTo(BigDecimal.ZERO) == 0) {
            facture.setStatut(StatutFacture.PAYEE);
            log.info("Facture {} entièrement payée", facture.getNumeroFacture());
        } else if (facture.getMontantTtc().compareTo(BigDecimal.ZERO) > 0) {
            facture.setStatut(StatutFacture.PAYEE_PARTIELLEMENT);
        }
    }

    // === MÉTHODES SUPPLEMENTAIRES UTILES ===

    @Override
    @Transactional(readOnly = true)
    public Page<FactureResponse> findAll(Pageable pageable) {
        return factureRepository.findAll(pageable)
                .map(FactureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponse> getFacturesByClient(Long clientId) {
        return factureRepository.findByClientId(clientId).stream()
                .map(FactureMapper::toResponse)
                .toList();
    }

    @Override
    public FactureResponse annulerFacture(Long factureId, String raison) throws EntityNotFound {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> EntityNotFound.of("Facture non trouvée"));
        
        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new IllegalStateException("Impossible d'annuler une facture déjà payée");
        }
        
        facture.setStatut(StatutFacture.ANNULEE);
        facture.setDateModification(LocalDateTime.now());
        
        if (facture.getNotes() == null) {
            facture.setNotes("Annulée le " + LocalDate.now() + " : " + raison);
        } else {
            facture.setNotes(facture.getNotes() + "\nAnnulée le " + LocalDate.now() + " : " + raison);
        }
        
        Facture factureAnnulee = factureRepository.save(facture);
        log.info("Facture {} annulée", facture.getNumeroFacture());
        
        return FactureMapper.toResponse(factureAnnulee);
    }

}