package com.emak.crm.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produits")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,name="reference_sku")
    private String referenceSku;
    @Column(name="nom_produit")
    private String nomProduit;
    @Column(name="produit_description")
    private String description;
    private String categorie;
    private String famille;
    @Column(name="prix_unitaire_ht")
    private BigDecimal prixUnitaireHt;
    @Column(name="cout_unitaire")
    private BigDecimal coutUnitaire;
    @Builder.Default
    private Integer stock = 0;
    @Builder.Default
    private Boolean actif = true;
    @Builder.Default
    @Column(name="gere_stock")
    private boolean gereStock = true;
    @Builder.Default
    @Column(name="date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    @Builder.Default
    @Column(name="date_modification")
    private LocalDateTime dateModification = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}