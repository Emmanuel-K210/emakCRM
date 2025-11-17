package com.emak.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ligne_devis")
public class LigneDevis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer quantite = 1;
    
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2,name="prix_unitaire_ht")
    private BigDecimal prixUnitaireHt;
    
    @Column(precision = 5, scale = 2,name="tauxTva")
    @Builder.Default
    private BigDecimal tauxTva = new BigDecimal("20.00");
    
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal remise = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    @JoinColumn(name = "id_devis", nullable = false)
    private Devis devis;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;
    
    // Méthode utilitaire pour calculer le montant HT de la ligne
    public BigDecimal getMontantHt() {
        BigDecimal montantBase = prixUnitaireHt.multiply(BigDecimal.valueOf(quantite));
        if (remise.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal reduction = montantBase.multiply(remise.divide(new BigDecimal("100")));
            return montantBase.subtract(reduction);
        }
        return montantBase;
    }
    
    // Méthode utilitaire pour calculer le montant TTC de la ligne
    public BigDecimal getMontantTtc() {
        BigDecimal montantHt = getMontantHt();
        BigDecimal tva = montantHt.multiply(tauxTva.divide(new BigDecimal("100")));
        return montantHt.add(tva);
    }
}