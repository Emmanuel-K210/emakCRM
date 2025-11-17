package com.emak.crm.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.emak.crm.enums.Roles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "utilisateurs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

/**
 * @author emmanuel kouadio
 * Entite
 * service {@link com.emak.crm.service.impl.UtilisateurServiceImpl}
 * 
 */
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    
    @Column(unique = true)
    private String email;
    @Column(name="mot_passe")
    private String motPasse;
    private String telephone;
    @Enumerated(EnumType.STRING)
    @Column(name="user_role")
    private Roles role;
    private String equipe;
    @Column(name="date_embauche")
    private LocalDate dateEmbauche;
    @Builder.Default
    private Boolean actif = true;
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