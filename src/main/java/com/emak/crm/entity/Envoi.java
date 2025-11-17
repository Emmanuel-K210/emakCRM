package com.emak.crm.entity;

import java.time.LocalDateTime;

import com.emak.crm.enums.StatutEnvoi;
import com.emak.crm.enums.TypeEnvoi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "envois")
public class Envoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name="type_envoi")
    private TypeEnvoi type;
    
    private String objet;
    
    @Lob
    private String contenu;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutEnvoi statut = StatutEnvoi.ENVOYE;
    
    @Column(nullable = false,name="date_envoi")
    private LocalDateTime dateEnvoi;
    @Column(nullable = false,name="date_ouverture")
    private LocalDateTime dateOuverture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    
    @JoinColumn(name = "id_campagne", nullable = false)
    private Campagne campagne;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;
    
}