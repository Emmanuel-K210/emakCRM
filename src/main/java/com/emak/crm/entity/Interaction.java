package com.emak.crm.entity;

import java.time.LocalDateTime;

import com.emak.crm.enums.ResultatInteraction;
import com.emak.crm.enums.TypeInteraction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name="type_interaction")
    private TypeInteraction type;
    
    private String objet;
    @Column(name="compte_rendu")
    private String compteRendu;
    
    @Enumerated(EnumType.STRING)
    private ResultatInteraction resultat;
    @Column(name="date_interaction")
    private LocalDateTime dateInteraction;
    private Integer duree;
    
    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
    
    @ManyToOne
    @JoinColumn(name = "id_opportunite")
    private Opportunite opportunite;
    @Builder.Default
    @Column(name="date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    
}