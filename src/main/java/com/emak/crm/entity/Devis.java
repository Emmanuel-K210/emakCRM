package com.emak.crm.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.emak.crm.enums.StatutDevis;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "devis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true,name="numero_devis")
    private String numeroDevis;
    @Column(name="date_emission")
    private LocalDate dateEmission;
    @Column(name="date_validite")
    private LocalDate dateValidite;
    @Column(name="montant_ht")
    private BigDecimal montantHt;
    @Column(name="montant_tva")
    private BigDecimal montantTva;
    @Column(name="montant_ttc")
    private BigDecimal montantTtc;
    
    @Builder.Default
    @Column(name="taux_tva")
    private BigDecimal tauxTva = new BigDecimal("20.00");
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDevis statut = StatutDevis.BROUILLON;
    
    @Column(length = 1000,name="condition_generales")
    private String conditionsGenerales;
    
    @Column(length = 500)
    private String notes;
    
    @ManyToOne
    @JoinColumn(name = "id_opportunite")
    private Opportunite opportunite;
    
    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
    
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneDevis> lignes = new ArrayList<>();
    
    @Builder.Default
    @Column(name="date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Builder.Default
    @Column(name="date_modification")
    private LocalDateTime dateModification = LocalDateTime.now();
    @Builder.Default    
    @Column(name="deja_facture")
    private Boolean dejaFacture = false;
    
    @Transient
    private Integer joursRestants;
    
    @Transient
    private Boolean modifiable;
    
    @Transient
    private Boolean convertibleEnFacture;

    @PrePersist
    @PreUpdate
    public void calculerTotaux() {
        if (this.montantHt == null) {
            this.montantHt = calculerTotalHt();
        }
        if (this.montantTva == null) {
            this.montantTva = calculerMontantTva();
        }
        if (this.montantTtc == null) {
            this.montantTtc = calculerTotalTtc();
        }
        this.dateModification = LocalDateTime.now();
    }
    
    // Méthode utilitaire pour ajouter une ligne
    public void addLigne(LigneDevis ligne) {
        if (ligne != null) {
            lignes.add(ligne);
            ligne.setDevis(this);
            recalculerTotaux();
        }
    }
    
    // Méthode utilitaire pour supprimer une ligne
    public void removeLigne(LigneDevis ligne) {
        if (ligne != null) {
            lignes.remove(ligne);
            ligne.setDevis(null);
            recalculerTotaux();
        }
    }
    
    // Méthode pour recalculer tous les totaux
    public void recalculerTotaux() {
        this.montantHt = calculerTotalHt();
        this.montantTva = calculerMontantTva();
        this.montantTtc = calculerTotalTtc();
    }
    
    // Méthode utilitaire pour calculer le total HT
    public BigDecimal calculerTotalHt() {
        return lignes.stream()
                .map(LigneDevis::getMontantHt)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    // Méthode pour calculer le montant de la TVA
    public BigDecimal calculerMontantTva() {
        BigDecimal totalHt = calculerTotalHt();
        return totalHt.multiply(tauxTva.divide(new BigDecimal("100")))
                     .setScale(2, RoundingMode.HALF_UP);
    }
    
    // Méthode utilitaire pour calculer le total TTC
    public BigDecimal calculerTotalTtc() {
        BigDecimal totalHt = calculerTotalHt();
        BigDecimal montantTva = calculerMontantTva();
        return totalHt.add(montantTva)
                     .setScale(2, RoundingMode.HALF_UP);
    }
    
    // Méthode pour vérifier si le devis est modifiable
    public boolean isModifiable() {
        return statut == StatutDevis.BROUILLON || 
               statut == StatutDevis.EN_ATTENTE ||
               statut == StatutDevis.MODIFIE;
    }
    
    // Méthode pour vérifier si le devis est convertible en facture
    public boolean isConvertibleEnFacture() {
        return statut == StatutDevis.ACCEPTE && 
               !Boolean.TRUE.equals(dejaFacture) &&
               dateValidite != null &&
               !dateValidite.isBefore(LocalDate.now());
    }
    
    // Méthode pour calculer les jours restants avant expiration
    public Integer getJoursRestants() {
        if (dateValidite == null) {
            return null;
        }
        long jours = LocalDate.now().until(dateValidite).getDays();
        return Math.max(0, (int) jours);
    }
    
    // Méthode pour vérifier si le devis est expiré
    public boolean isExpire() {
        return dateValidite != null && dateValidite.isBefore(LocalDate.now());
    }
    
    // Méthode pour accepter le devis
    public void accepter() {
        if (this.statut == StatutDevis.EN_ATTENTE || this.statut == StatutDevis.MODIFIE) {
            this.statut = StatutDevis.ACCEPTE;
            this.dateModification = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Seuls les devis EN_ATTENTE ou MODIFIE peuvent être acceptés");
        }
    }
    
    // Méthode pour refuser le devis
    public void refuser(String raison) {
        if (this.statut == StatutDevis.EN_ATTENTE || this.statut == StatutDevis.MODIFIE) {
            this.statut = StatutDevis.REFUSE;
            this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                        "Refusé le " + LocalDate.now() + " : " + raison;
            this.dateModification = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Seuls les devis EN_ATTENTE ou MODIFIE peuvent être refusés");
        }
    }
    
    // Méthode pour marquer comme envoyé au client
    public void marquerEnvoye() {
        if (this.statut == StatutDevis.BROUILLON) {
            this.statut = StatutDevis.EN_ATTENTE;
            this.dateModification = LocalDateTime.now();
        }
    }
    
    // Méthode pour dupliquer le devis
    public Devis dupliquer() {
        Devis duplicate = Devis.builder()
                .dateEmission(LocalDate.now())
                .dateValidite(LocalDate.now().plusDays(30))
                .tauxTva(this.tauxTva)
                .statut(StatutDevis.BROUILLON)
                .conditionsGenerales(this.conditionsGenerales)
                .opportunite(this.opportunite)
                .utilisateur(this.utilisateur)
                .build();
        
        // Dupliquer les lignes
        this.lignes.forEach(ligne -> {
            LigneDevis ligneDuplicate = LigneDevis.builder()
                    .description(ligne.getDescription())
                    .prixUnitaireHt(ligne.getPrixUnitaireHt())
                    .quantite(ligne.getQuantite())
                    .tauxTva(ligne.getTauxTva())
                    .build();
            duplicate.addLigne(ligneDuplicate);
        });
        
        return duplicate;
    }
    
    @Override
    public String toString() {
        return "Devis{" +
                "id=" + id +
                ", numeroDevis='" + numeroDevis + '\'' +
                ", statut=" + statut +
                ", montantTtc=" + montantTtc +
                ", dateValidite=" + dateValidite +
                '}';
    }
}