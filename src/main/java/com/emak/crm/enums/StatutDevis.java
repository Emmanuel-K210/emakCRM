package com.emak.crm.enums;

import java.util.Arrays;
import java.util.List;

public enum StatutDevis {
    BROUILLON("Brouillon", "Devis en cours de création", "secondary", false, true),
    ENVOYE("Envoyé", "Devis envoyé au client", "info", false, false),
    EN_ATTENTE("En attente", "En attente de réponse du client", "warning", false, false),
    ACCEPTE("Accepté", "Devis accepté par le client", "success", true, false),
    REFUSE("Refusé", "Devis refusé par le client", "danger", true, false),
    EXPIRE("Expiré", "Devis expiré", "dark", true, false),
    ANNULE("Annulé", "Devis annulé", "light", true, false),
    MODIFIE("Modifié", "Devis modifié après envoi", "primary", false, true),
    CONVERTI_EN_FACTURE("Converti en facture", "Devis converti en facture", "success", true, false);

    private final String libelle;
    private final String description;
    private final String couleur; // Pour l'UI
    private final boolean finalState; // État final non modifiable
    private final boolean modifiable; // Peut être modifié

    StatutDevis(String libelle, String description, String couleur, boolean finalState, boolean modifiable) {
        this.libelle = libelle;
        this.description = description;
        this.couleur = couleur;
        this.finalState = finalState;
        this.modifiable = modifiable;
    }

    // Getters
    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
    public String getCouleur() { return couleur; }
    public boolean isFinalState() { return finalState; }
    public boolean isModifiable() { return modifiable; }

    // Méthodes métier utilitaires
    public boolean peutEtreModifie() {
        return this.modifiable;
    }

    public boolean estFinal() {
        return this.finalState;
    }

    public boolean peutEtreEnvoye() {
        return this == BROUILLON || this == MODIFIE;
    }

    public boolean peutEtreAccepte() {
        return this == ENVOYE || this == EN_ATTENTE || this == MODIFIE;
    }

    public boolean peutEtreRefuse() {
        return this == ENVOYE || this == EN_ATTENTE || this == MODIFIE;
    }

    public boolean peutEtreConvertiEnFacture() {
        return this == ACCEPTE;
    }

    public boolean estEnCours() {
        return this == BROUILLON || this == ENVOYE || this == EN_ATTENTE || this == MODIFIE;
    }

    public boolean estTermine() {
        return this == ACCEPTE || this == REFUSE || this == EXPIRE || this == ANNULE || this == CONVERTI_EN_FACTURE;
    }

    // Transitions autorisées
    public List<StatutDevis> getTransitionsAutorisees() {
        return switch (this) {
            case BROUILLON -> Arrays.asList(ENVOYE, ANNULE);
            case ENVOYE -> Arrays.asList(EN_ATTENTE, ACCEPTE, REFUSE, EXPIRE, ANNULE);
            case EN_ATTENTE -> Arrays.asList(ACCEPTE, REFUSE, EXPIRE, ANNULE);
            case MODIFIE -> Arrays.asList(ENVOYE, ANNULE);
            case ACCEPTE -> Arrays.asList(CONVERTI_EN_FACTURE);
            case REFUSE, EXPIRE, ANNULE, CONVERTI_EN_FACTURE -> List.of(); // Aucune transition depuis les états finaux
        };
    }

    // Validation d'une transition
    public boolean peutTransitionVers(StatutDevis nouveauStatut) {
        return getTransitionsAutorisees().contains(nouveauStatut);
    }

    // Statuts actifs (pour les relances)
    public static List<StatutDevis> getStatutsActifs() {
        return Arrays.asList(BROUILLON, ENVOYE, EN_ATTENTE, MODIFIE);
    }

    // Statuts terminés
    public static List<StatutDevis> getStatutsTermines() {
        return Arrays.asList(ACCEPTE, REFUSE, EXPIRE, ANNULE, CONVERTI_EN_FACTURE);
    }

    // Statuts pour relance client
    public static List<StatutDevis> getStatutsARelancer() {
        return Arrays.asList(ENVOYE, EN_ATTENTE);
    }

    // Statuts modifiables
    public static List<StatutDevis> getStatutsModifiables() {
        return Arrays.asList(BROUILLON, ENVOYE, EN_ATTENTE, MODIFIE);
    }

    // Trouver par libellé
    public static StatutDevis parLibelle(String libelle) {
        return Arrays.stream(values())
                .filter(statut -> statut.getLibelle().equalsIgnoreCase(libelle))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Statut inconnu: " + libelle));
    }

    // Vérifier si un statut est valide
    public static boolean estValide(String statut) {
        return Arrays.stream(values())
                .anyMatch(s -> s.name().equalsIgnoreCase(statut) || s.getLibelle().equalsIgnoreCase(statut));
    }

    // Prochain statut suggéré après envoi
    public static StatutDevis getStatutApresEnvoi() {
        return EN_ATTENTE;
    }

    // Méthode pour l'UI - obtenir la classe CSS Bootstrap
    public String getClasseCss() {
        return "badge bg-" + this.couleur;
    }

    // Méthode pour l'UI - obtenir l'icône
    public String getIcone() {
        return switch (this) {
            case BROUILLON -> "📝";
            case ENVOYE, EN_ATTENTE -> "⏳";
            case MODIFIE -> "✏️";
            case ACCEPTE, CONVERTI_EN_FACTURE -> "✅";
            case REFUSE -> "❌";
            case EXPIRE -> "⌛";
            case ANNULE -> "🚫";
        };
    }

    @Override
    public String toString() {
        return this.libelle;
    }
}