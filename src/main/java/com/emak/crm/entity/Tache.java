package com.emak.crm.entity;

import java.time.LocalDateTime;

import com.emak.crm.enums.PrioriteTache;
import com.emak.crm.enums.StatutTache;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "taches")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titre;
    @Column(name="tache_description")
    private String description;
    @Column(name="date_debut")
    private LocalDateTime dateDebut;
    @Column(name="date_echeance")
    private LocalDateTime dateEcheance;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PrioriteTache priorite = PrioriteTache.NORMALE;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatutTache statut = StatutTache.A_FAIRE;
    
    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
    
    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "id_opportunite")
    private Opportunite opportunite;
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