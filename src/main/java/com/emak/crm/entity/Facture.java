package com.emak.crm.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.emak.crm.enums.StatutFacture;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "factures")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true,name="numero_facture")
    private String numeroFacture;
    @Column(name="date_facture")
    private LocalDate dateFacture;
    @Column(name="date_echeance")
    private LocalDate dateEcheance;
    @Column(name="montant_ht")
    private BigDecimal montantHt;
    @Column(name="montant_ttc")
    private BigDecimal montantTtc;
    @Column(name="montant_tva")
    private BigDecimal montantTva;
    @Column(name="taux_tva")
    @Builder.Default
    private BigDecimal tauxTva = new BigDecimal("20.00");
    @Column(name="montant_restant")
    private BigDecimal montantRestant;
    @Builder.Default
    private String notes = "";
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutFacture statut = StatutFacture.BROUILLON;
    
    @ManyToOne
    @JoinColumn(name = "id_devis")
    private Devis devis;
    
    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
    @Builder.Default
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    private List<Paiement> paiements = new ArrayList<>();
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();
}