package com.emak.crm.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.emak.crm.enums.StatutCampagne;
import com.emak.crm.enums.TypeCampagne;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "campagnes")
public class Campagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false,name="nom_campagne")
    private String nomCampagne;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name="type_campagne")
    private TypeCampagne type;
    @Column(name="date_debut")
    private LocalDate dateDebut;
    @Column(name="date_fin")
    private LocalDate dateFin;
    private BigDecimal budget;
    private String objectif;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutCampagne statut = StatutCampagne.PLANIFIEE;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal tauxConversion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_responsable", nullable = false)
    private Utilisateur utilisateurResponsable;
    
    @ManyToMany
    @JoinTable(
        name = "campagne_liste",
        joinColumns = @JoinColumn(name = "id_campagne"),
        inverseJoinColumns = @JoinColumn(name = "id_liste")
    )
    @Builder.Default
    private List<ListeDiffusion> listes = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL)
    private List<Envoi> envois = new ArrayList<>();
    @Builder.Default
    @Column(name="date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    
}