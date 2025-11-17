package com.emak.crm.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emak.crm.dto.DevisRequest;
import com.emak.crm.dto.DevisResponse;
import com.emak.crm.dto.LigneDevisRequest;
import com.emak.crm.entity.Client;
import com.emak.crm.entity.Devis;
import com.emak.crm.entity.Facture;
import com.emak.crm.entity.LigneDevis;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.enums.StatutDevis;
import com.emak.crm.enums.StatutFacture;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.DevisMapper;
import com.emak.crm.repository.DevisRepository;
import com.emak.crm.repository.FactureRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.service.DevisService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class DevisServiceImpl implements DevisService {

    private final DevisRepository devisRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final FactureRepository factureRepository;

    private Devis getById(Number id) throws EntityNotFound {
        return devisRepository.findById(id.longValue())
                .orElseThrow(() -> EntityNotFound.of("Devis non trouvé avec l'id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DevisResponse> findAll(Pageable pageable) {
        return devisRepository.findAll(pageable)
                .map(DevisMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisResponse> findAll() {
        return devisRepository.findAll().stream()
                .map(DevisMapper::toResponse)
                .toList();
    }

    /**
     * CRÉATION D'UN DEVIS
     * Métier : Établir une proposition commerciale formalisée
     * Workflow : Sélection client → Ajout lignes → Calcul automatique → Génération numéro
     * Règles : Client obligatoire, lignes avec prix, validité
     */
    @Override
    public DevisResponse save(DevisRequest requete) {
        
        // 1. Validation de l'opportunité
        Opportunite opportunite = opportuniteRepository.findById(requete.idOpportunite())
            .orElseThrow(() -> new IllegalArgumentException("L'opportunité fournie n'a pas été trouvée"));
        
        // 2. Validation du client via l'opportunité
        if (opportunite.getClient() == null) {
            throw new IllegalArgumentException("L'opportunité doit être associée à un client");
        }
        
        // 3. Validation des lignes de devis
        if (requete.lignes() == null || requete.lignes().isEmpty()) {
            throw new IllegalArgumentException("Le devis doit contenir au moins une ligne");
        }
        
        // 4. Validation des prix sur les lignes
        for (LigneDevisRequest ligne : requete.lignes()) {
            if (ligne.prixUnitaireHt() == null || ligne.prixUnitaireHt().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Chaque ligne doit avoir un prix unitaire positif");
            }
            if (ligne.quantite() == null || ligne.quantite() <= 0) {
                throw new IllegalArgumentException("Chaque ligne doit avoir une quantité positive");
            }
        }
        
        // 5. Conversion en entité
        Devis devis = DevisMapper.toEntity(requete);
        
        // 6. Génération automatique du numéro de devis
        String numeroDevis = genererNumeroDevis();
        devis.setNumeroDevis(numeroDevis);
        
        // 7. Définition de la date de validité par défaut si non fournie
        if (devis.getDateValidite() == null) {
            devis.setDateValidite(LocalDate.now().plusDays(30));
        }
        
        // 8. Association avec l'opportunité 
        devis.setOpportunite(opportunite);
        
        // 9. Les totaux sont calculés automatiquement via @PrePersist dans l'entité
        
        // 10. Sauvegarde
        Devis devisSauvegarde = devisRepository.save(devis);
        log.info("Devis créé avec le numéro: {}", numeroDevis);
        
        return DevisMapper.toResponse(devisSauvegarde);
    }

    /**
     * Génère un numéro de devis unique selon le format métier
     */
    private String genererNumeroDevis() {
        String prefixe = "DEVIS";
        int annee = LocalDate.now().getYear();
        Long sequence = devisRepository.countByDateCreationYear(annee) + 1;
        return String.format("%s-%d-%04d", prefixe, annee, sequence);
    }

    /**
     * CONSULTATION DEVIS
     * Métier : Voir le détail complet d'une proposition
     * Usage : Relance client, modification, conversion facture
     */
    @Override
    @Transactional(readOnly = true)
    public DevisResponse findById(Number id) throws EntityNotFound {
        Devis devis = getById(id);
        enrichirAvecDonneesMetier(devis);
        return DevisMapper.toResponse(devis);
    }

    /**
     * Enrichit le devis avec des données métier selon les besoins
     */
    private void enrichirAvecDonneesMetier(Devis devis) {
        // 1. Pour la relance client : calculer les jours restants
        if (devis.getDateValidite() != null) {
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), devis.getDateValidite());
            devis.setJoursRestants(joursRestants > 0 ? (int) joursRestants : 0);
        }
        
        // 2. Pour la modification : vérifier si modifiable
        devis.setModifiable(peutEtreModifie(devis.getStatut()));
        
        // 3. Pour la conversion facture : vérifier si convertible
        devis.setConvertibleEnFacture(peutEtreConvertiEnFacture(devis));
    }

    /**
     * Vérifie si le devis peut être modifié selon son statut
     */
    private boolean peutEtreModifie(StatutDevis statut) {
        return statut == StatutDevis.BROUILLON || 
               statut == StatutDevis.ENVOYE || 
               statut == StatutDevis.EN_ATTENTE;
    }

    /**
     * Vérifie si le devis peut être converti en facture
     */
    private boolean peutEtreConvertiEnFacture(Devis devis) {
        return devis.getStatut() == StatutDevis.ACCEPTE && 
               !Boolean.TRUE.equals(devis.getDejaFacture()) && 
               devis.getDateValidite() != null &&
               !devis.getDateValidite().isBefore(LocalDate.now());
    }

    /**
     * MISE À JOUR DEVIS
     * Métier : Modifier une proposition avant acceptation
     * Workflow : Vérification statut → Recalcul totals → Historique
     */
    @Override
    public DevisResponse update(Number id, DevisRequest requete) throws EntityNotFound {
        // 1. Récupération et vérification du devis
        Devis devis = getById(id);
        
        // 2. Vérification que le devis peut être modifié
        if (!peutEtreModifie(devis.getStatut())) {
            throw new IllegalStateException("Impossible de modifier un devis avec le statut: " + devis.getStatut());
        }
        
        // 3. Mise à jour de l'opportunité (et donc du client lié)
        if (requete.idOpportunite() != null) {
            Opportunite opp = opportuniteRepository.findById(requete.idOpportunite())
                    .orElseThrow(() -> EntityNotFound.of("Opportunité non trouvée"));
            devis.setOpportunite(opp);
        }
        
        // 4. Mise à jour des champs de base
        if (requete.statut() != null) {
            devis.setStatut(StatutDevis.valueOf(requete.statut()));
        }
        
        if (requete.dateValidite() != null) {
            devis.setDateValidite(requete.dateValidite());
        }
        
        // 5. Mise à jour des lignes si fournies
        if (requete.lignes() != null && !requete.lignes().isEmpty()) {
            mettreAJourLignesDevis(devis, requete.lignes());
        }
        
        // 6. Date de modification
        devis.setDateModification(LocalDateTime.now());
        
        // Les totaux sont recalculés automatiquement via @PreUpdate
        
        Devis devisMisAJour = devisRepository.save(devis);
        log.info("Devis {} mis à jour", devis.getNumeroDevis());
        
        return DevisMapper.toResponse(devisMisAJour);
    }

    /**
     * Met à jour les lignes du devis
     */
    private void mettreAJourLignesDevis(Devis devis, List<LigneDevisRequest> nouvellesLignes) {
        // Supprimer les anciennes lignes
        devis.getLignes().clear();
        
        // Ajouter les nouvelles lignes
        for (LigneDevisRequest ligneRequest : nouvellesLignes) {
            LigneDevis ligne = LigneDevis.builder()
                    .description(ligneRequest.description())
                    .prixUnitaireHt(ligneRequest.prixUnitaireHt())
                    .quantite(ligneRequest.quantite())
                    .tauxTva(ligneRequest.tauxTva() != null ? ligneRequest.tauxTva() : new BigDecimal("20.00"))
                    .build();
            devis.addLigne(ligne);
        }
    }
    
    
    /**
     * SUPPRESSION DEVIS
     * Métier : Annuler une proposition devenue obsolète
     * Règles : Impossible si déjà converti en facture
     */
    @Override
    public void deleteById(Number id) throws EntityNotFound {
        Devis devis = getById(id);
        
        // Vérifier qu'on ne supprime pas un devis accepté ou facturé
        if (devis.getStatut() == StatutDevis.ACCEPTE || Boolean.TRUE.equals(devis.getDejaFacture())) {
            throw new IllegalStateException("Impossible de supprimer un devis accepté ou déjà facturé");
        }
        
        devisRepository.delete(devis);
        log.info("Devis {} supprimé", devis.getNumeroDevis());
    }

    /**
     * Consultation pour conversion en facture
     */
    @Override
    @Transactional(readOnly = true)
    public DevisResponse findByIdPourConversion(Number id) throws EntityNotFound {
        Devis devis = getById(id);
        
        // Validation pour la conversion
        if (!peutEtreConvertiEnFacture(devis)) {
            throw new IllegalStateException("Ce devis ne peut pas être converti en facture. " +
                    "Statut: " + devis.getStatut() + 
                    ", Validité: " + devis.getDateValidite() +
                    ", Déjà facturé: " + devis.getDejaFacture());
        }
        
        return DevisMapper.toResponse(devis);
    }

    /**
     * ENVOYER LE DEVIS
     * Métier : Marquer le devis comme envoyé au client
     * Workflow : Changement statut → Notification → Début délai validité
     * @throws EntityNotFound 
     */
    @Override
    public DevisResponse envoyerDevis(Long id) throws EntityNotFound {
        // Récupération du devis
        Devis devis = getById(id);        
        // 1. Validation : vérifier que le devis peut être envoyé
        if (!peutEtreEnvoye(devis)) {
            throw new IllegalStateException("Le devis ne peut pas être envoyé. Statut actuel: " + devis.getStatut());
        }
        
        // 2. Validation : vérifier que le devis a au moins une ligne
        if (devis.getLignes() == null || devis.getLignes().isEmpty()) {
            throw new IllegalStateException("Impossible d'envoyer un devis sans lignes");
        }
        
        // 3. Validation : vérifier que les totaux sont calculés
        if (devis.getMontantTtc() == null || devis.getMontantTtc().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Les totaux du devis doivent être calculés avant envoi");
        }
        
        // 4. Changement du statut
        devis.setStatut(StatutDevis.ENVOYE);
        devis.setDateModification(LocalDateTime.now());
        
        // 5. Définir la date d'émission si ce n'est pas déjà fait
        if (devis.getDateEmission() == null) {
            devis.setDateEmission(LocalDate.now());
        }
        
        // 6. Définir la date de validité si ce n'est pas déjà fait
        if (devis.getDateValidite() == null) {
            devis.setDateValidite(LocalDate.now().plusDays(30)); // 30 jours par défaut
        }
        
        // 7. Sauvegarder le devis
        Devis devisEnvoye = devisRepository.save(devis);
        
        // 8. Notifier le client (implémentation basique)
        notifierClient(devisEnvoye);
        
        log.info("Devis {} envoyé au client {}", devisEnvoye.getNumeroDevis(), 
                 devisEnvoye.getOpportunite().getClient().getNom());
        
        return DevisMapper.toResponse(devisEnvoye);
    }

    /**
     * Vérifie si le devis peut être envoyé
     */
    private boolean peutEtreEnvoye(Devis devis) {
        return devis.getStatut() == StatutDevis.BROUILLON || 
               devis.getStatut() == StatutDevis.MODIFIE;
    }

    /**
     * Notification du client (version simplifiée)
     */
    private void notifierClient(Devis devis) {
        try {
            Client client = devis.getOpportunite().getClient();
            
            // Log pour simulation - à remplacer par un vrai service de notification
            log.info("=== NOTIFICATION CLIENT ===");
            log.info("Destinataire: {} <{}>", client.getNom(), client.getEmail());
            log.info("Sujet: Votre devis {}", devis.getNumeroDevis());
            log.info("Message: Bonjour {}, votre devis d'un montant de {} € a été envoyé. Validité jusqu'au {}",
                    client.getNom(),
                    devis.getMontantTtc(),
                    devis.getDateValidite());
            log.info("=== FIN NOTIFICATION ===");
            
            // Ici vous pourriez appeler :
            // - Un service d'email
            // - Un service de SMS
            // - Une intégration avec un système de messagerie
            // emailService.envoyerDevis(client.getEmail(), devis);
            
        } catch (Exception e) {
            log.warn("Échec de la notification du client pour le devis {}", devis.getNumeroDevis(), e);
            // On ne bloque pas le processus principal si la notification échoue
        }
    }

    /**
     * Version alternative avec envoi d'email (exemple)
     */
    private void notifierClientAvecEmail(Devis devis) {
        // Exemple d'implémentation avec un service d'email
        /*
        Client client = devis.getOpportunite().getClient();
        
        EmailMessage email = EmailMessage.builder()
                .to(client.getEmail())
                .subject("Votre devis " + devis.getNumeroDevis())
                .template("devis-envoye")
                .variables(Map.of(
                    "clientNom", client.getNom(),
                    "devisNumero", devis.getNumeroDevis(),
                    "devisMontant", devis.getMontantTtc(),
                    "devisValidite", devis.getDateValidite(),
                    "devisLignes", devis.getLignes()
                ))
                .build();
        
        emailService.sendEmail(email);
        */
    }
    
    /**
     * CONVERTIR EN FACTURE
     * Métier : Transformer un devis accepté en facture
     * Workflow : Validation acceptation → Création facture → Numérotation
     * @throws EntityNotFound 
     */
    @Override
    public DevisResponse convertirEnFacture(Long id) throws EntityNotFound {
        // Récupération du devis
        Devis devis = devisRepository.findById(id)
                .orElseThrow(() -> EntityNotFound.of("Devis non trouvé avec l'id: " + id));
        
        // 1. Validation : vérifier que le devis peut être converti en facture
        validerConversionFacture(devis);
        
        // 2. Création de la facture à partir du devis
        Facture facture = creerFactureFromDevis(devis);
        
        // 3. Sauvegarder la facture
        Facture factureCreee = factureRepository.save(facture);
        
        // 4. Mettre à jour le devis
        devis.setDejaFacture(true);
        devis.setStatut(StatutDevis.CONVERTI_EN_FACTURE);
        devis.setDateModification(LocalDateTime.now());
        
        Devis devisMisAJour = devisRepository.save(devis);
        
        // 5. Notifier (optionnel)
        notifierConversionFacture(devisMisAJour, factureCreee);
        
        log.info("Devis {} converti en facture {}", devis.getNumeroDevis(), factureCreee.getNumeroFacture());
        
        return DevisMapper.toResponse(devisMisAJour);
    }

    /**
     * Validation des règles métier pour la conversion en facture
     */
    private void validerConversionFacture(Devis devis) {
        // 1. Vérifier le statut
        if (devis.getStatut() != StatutDevis.ACCEPTE) {
            throw new IllegalStateException("Seuls les devis acceptés peuvent être convertis en facture. Statut actuel: " + devis.getStatut());
        }
        
        // 2. Vérifier que le devis n'est pas déjà facturé
        if (Boolean.TRUE.equals(devis.getDejaFacture())) {
            throw new IllegalStateException("Ce devis a déjà été converti en facture");
        }
        
        // 3. Vérifier la validité
        if (devis.getDateValidite() == null) {
            throw new IllegalStateException("Le devis doit avoir une date de validité");
        }
        
        if (devis.getDateValidite().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Le devis est expiré. Date de validité: " + devis.getDateValidite());
        }
        
        // 4. Vérifier les totaux
        if (devis.getMontantTtc() == null || devis.getMontantTtc().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Le devis doit avoir un montant TTC positif");
        }
        
        // 5. Vérifier les lignes
        if (devis.getLignes() == null || devis.getLignes().isEmpty()) {
            throw new IllegalStateException("Le devis doit contenir au moins une ligne");
        }
        
        // 6. Vérifier le client
        if (devis.getOpportunite() == null || devis.getOpportunite().getClient() == null) {
            throw new IllegalStateException("Le devis doit être associé à un client");
        }
    }

    /**
     * Crée une facture à partir d'un devis
     */
    private Facture creerFactureFromDevis(Devis devis) {
        Facture facture = Facture.builder()
                .numeroFacture(genererNumeroFacture())
                .dateFacture(LocalDate.now())
                .dateEcheance(LocalDate.now().plusDays(30)) // 30 jours par défaut
                .montantHt(devis.getMontantHt())
                .montantTva(devis.getMontantTva())
                .montantTtc(devis.getMontantTtc())
                .statut(StatutFacture.EMISE)
                .client(devis.getOpportunite().getClient())
                .utilisateur(devis.getUtilisateur())
                .notes("Facture générée à partir du devis: " + devis.getNumeroDevis())
                .build();
        
        
        
        return facture;
    }


    /**
     * Génère un numéro de facture unique
     */
    private String genererNumeroFacture() {
        String prefixe = "FACT";
        int annee = LocalDate.now().getYear();
        Long sequence = factureRepository.countByDateFacturationYear(annee) + 1;
        return String.format("%s-%d-%04d", prefixe, annee, sequence);
    }

    /**
     * Notification de la conversion
     */
    private void notifierConversionFacture(Devis devis, Facture facture) {
        try {
            Client client = devis.getOpportunite().getClient();
            
            log.info("=== CONVERSION FACTURE ===");
            log.info("Devis {} converti en facture {}", devis.getNumeroDevis(), facture.getNumeroFacture());
            log.info("Client: {}", client.getNom());
            log.info("Montant: {} €", facture.getMontantTtc());
            log.info("=== FIN NOTIFICATION ===");
            
        } catch (Exception e) {
            log.warn("Échec de la notification pour la conversion du devis en facture", e);
        }
    }
    
   
}